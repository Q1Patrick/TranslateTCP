// ================================================================
// TestClient.cs  —  C# Client test server Python
// Gửi văn bản tiếng Anh → nhận bản dịch tiếng Việt
//
// Cách dùng:
//   1. Chạy server.py trước
//   2. Chạy file C# này
// ================================================================
using System.Net.Sockets;
using System.Text;

const string HOST = "127.0.0.1";
const int    PORT = 9999;

string englishText = """
Hello! This is a test message sent from C# client.
I want to translate this text to Vietnamese.
The weather today is very nice.
""";

Console.WriteLine("=== C# Translation Client ===");
Console.WriteLine($"Kết nối đến {HOST}:{PORT}...");

try
{
    using var client = new TcpClient(HOST, PORT);
    using var stream = client.GetStream();

    // ── Gửi: 4-byte header + nội dung UTF-8 ──────────────────
    byte[] contentBytes = Encoding.UTF8.GetBytes(englishText);
    byte[] headerBytes  = BitConverter.GetBytes(contentBytes.Length);

    // Server Python dùng big-endian → phải đảo nếu máy là little-endian
    if (BitConverter.IsLittleEndian)
        Array.Reverse(headerBytes);

    await stream.WriteAsync(headerBytes);
    await stream.WriteAsync(contentBytes);
    Console.WriteLine($"[->] Đã gửi {contentBytes.Length} bytes.");

    // ── Nhận: đọc 4-byte header trước ────────────────────────
    byte[] respHeader = new byte[4];
    await ReadExactAsync(stream, respHeader);

    if (BitConverter.IsLittleEndian)
        Array.Reverse(respHeader);

    int respLength = BitConverter.ToInt32(respHeader);
    Console.WriteLine($"[<-] Sẽ nhận {respLength} bytes...");

    byte[] respBytes = new byte[respLength];
    await ReadExactAsync(stream, respBytes);

    string result = Encoding.UTF8.GetString(respBytes);
    Console.WriteLine("\n=== KẾT QUẢ DỊCH ===");
    Console.WriteLine(result);
}
catch (Exception ex)
{
    Console.WriteLine($"Lỗi: {ex.Message}");
}

// Helper: đọc đúng n byte
static async Task ReadExactAsync(NetworkStream stream, byte[] buffer)
{
    int received = 0;
    while (received < buffer.Length)
    {
        int n = await stream.ReadAsync(buffer, received, buffer.Length - received);
        if (n == 0) throw new Exception("Server ngắt kết nối.");
        received += n;
    }
}
