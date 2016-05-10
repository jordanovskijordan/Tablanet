import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ServerMain {

	public static final int PORT = 5222;

	public static void zatvoriKonekcija(String client, Socket client1, Socket client2) {
		System.err.println(client + " isklucen");
		try {
			client1.close();
			client2.close();
		} catch (IOException ex) {
		}
	}

	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		Socket client1 = null, client2 = null;
		ExecutorService executorService = null;

		try {
			serverSocket = new ServerSocket(PORT);
			executorService = Executors.newCachedThreadPool();

		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				serverSocket.close();
			} catch (IOException e) {
			}
		}

		while (true) {
			try {
				serverSocket.setSoTimeout(0);
				client1 = serverSocket.accept();
				/*
				 * client1 e veke konektiran i ceka 10 sekundi ceka da se
				 * konektira nekoj ako ne frla SocketTimeoutException
				 * client2.setSoTimeout(10000);
				 */
				serverSocket.setSoTimeout(100000);
				client2 = serverSocket.accept();

				InputStream client1InputStream = null;
				OutputStream client1OutputStream = null;
				InputStream client2InputStream = null;
				OutputStream client2OutputStream = null;

				try {
					client1InputStream = client1.getInputStream();
					client1OutputStream = client1.getOutputStream();
					client2InputStream = client2.getInputStream();
					client2OutputStream = client2.getOutputStream();

					client1.setSoTimeout(1000000);
					client2.setSoTimeout(1000000);

				} catch (Exception e) {
					try {
						client1OutputStream.write("nema igraci".getBytes());
						client2OutputStream.write("nema igraci".getBytes());
					} catch (IOException e1) {
					}
					e.printStackTrace();
					zatvoriKonekcija("nekoj", client1, client2);
					continue;
				}
				int size = 0;
				byte[] bytes = new byte[5000];
				try {
					client1OutputStream.write("1".getBytes());
					size = client1InputStream.read(bytes);
					System.out.println("pomina klient 1 read ok");

				} catch (IOException e) {
					try {
						client2OutputStream.write("nema igraci".getBytes());
						System.out.println("klient 1 disconnected posle primi ok");
					} catch (IOException e1) {
					}
					zatvoriKonekcija("client1", client1, client2);
					continue;
				}
				if (size == -1) {
					try {
						client2OutputStream.write("nema igraci".getBytes());
					} catch (IOException e) {
					}
					System.out.println("klient 1 disconnected posle primi ok");

					zatvoriKonekcija("client1", client1, client2);
					continue;
				}

				try {
					client2OutputStream.write("2".getBytes());
					size = client2InputStream.read(bytes);
					System.out.println("pomina klient 2 read ok");

				} catch (IOException e) {
					try {
						client1OutputStream.write("nema igraci".getBytes());
						System.out.println("klient 2 disconnected posle primi ok");
					} catch (IOException e1) {
					}
					zatvoriKonekcija("client2", client1, client2);
					continue;
				}
				if (size == -1) {
					try {
						client1OutputStream.write("nema igraci".getBytes());
						System.out.println("klient 2 disconnected posle primi ok");
					} catch (IOException e1) {
					}
					zatvoriKonekcija("client2", client1, client2);
					continue;
				}

				System.out.println(client1.getInetAddress().getHostAddress() + ":" + client1.getPort() + " vs "
						+ client2.getInetAddress().getHostAddress() + ":" + client2.getPort());

				try {
					executorService.execute(new ServerThread(client1, client2));
				} catch (RejectedExecutionException e) {
				}

			} catch (SocketTimeoutException e) {

				try {
					OutputStream outputStream = client1.getOutputStream();
					outputStream.write("nema igraci".getBytes());
					client1.close();
				} catch (IOException ex) {
				}

			} catch (SocketException e) {
			} catch (IOException e) {
				System.out.println("");
			}
		}

	}
}
