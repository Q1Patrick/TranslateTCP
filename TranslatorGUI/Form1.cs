using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Windows.Forms;

namespace TranslatorGUI
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void btnTranslate_Click(object sender, EventArgs e)
        {
            if (string.IsNullOrWhiteSpace(txtSource.Text))
            {
                MessageBox.Show("Vui lòng nhập văn bản cần dịch!");
                return;
            }

            // Đổi text nút để báo hiệu đang chờ xử lý
            btnTranslate.Text = "Đang dịch...";
            btnTranslate.Enabled = false;

            try
            {
                string host = "127.0.0.1";
                int port = 8888;
                TcpClient client = new TcpClient(host, port);
                NetworkStream stream = client.GetStream();

                // ================= LƯỢT GỬI (CLIENT -> SERVER) =================
                // 1. Mã hóa văn bản thành byte
                byte[] payloadToSend = Encoding.UTF8.GetBytes(txtSource.Text);

                // 2. Lấy chiều dài của mảng byte, chuyển sang chuẩn Network (Big-Endian)
                byte[] lengthPrefixToSend = BitConverter.GetBytes(IPAddress.HostToNetworkOrder(payloadToSend.Length));

                // 3. Gửi 4 byte chiều dài trước, sau đó gửi nội dung
                stream.Write(lengthPrefixToSend, 0, lengthPrefixToSend.Length);
                stream.Write(payloadToSend, 0, payloadToSend.Length);


                // ================= LƯỢT NHẬN (SERVER -> CLIENT) =================
                // 4. Đọc 4 byte đầu tiên để biết độ dài bản dịch
                byte[] lengthBuffer = new byte[4];
                int bytesRead = stream.Read(lengthBuffer, 0, 4);
                if (bytesRead < 4) throw new Exception("Mất kết nối trước khi nhận đủ header.");

                int expectedLength = IPAddress.NetworkToHostOrder(BitConverter.ToInt32(lengthBuffer, 0));

                // 5. Đọc đủ số byte nội dung bằng vòng lặp (Chống phân mảnh)
                byte[] payloadBuffer = new byte[expectedLength];
                int totalBytesRead = 0;
                while (totalBytesRead < expectedLength)
                {
                    int read = stream.Read(payloadBuffer, totalBytesRead, expectedLength - totalBytesRead);
                    if (read == 0) throw new Exception("Mất kết nối trong quá trình tải dữ liệu.");
                    totalBytesRead += read;
                }

                // 6. Hiển thị kết quả lên giao diện
                string translatedText = Encoding.UTF8.GetString(payloadBuffer);
                txtTranslated.Text = translatedText;

                stream.Close();
                client.Close();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Lỗi: " + ex.Message, "Lỗi Network");
            }
            finally
            {
                btnTranslate.Text = "Dịch";
                btnTranslate.Enabled = true;
            }
        }
    }
}