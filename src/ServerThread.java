
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import com.jordanbumbarjordanovski.tablanet.InputOutputObject;

public class ServerThread implements Runnable {
	Object tempinputoutput;
	Socket client1;
	Socket client2;

	ArrayList<Integer> kartivoraka1, kartivoraka2, kartinamasa;
	ArrayList<Integer> spil;

	public ServerThread(Socket c1, Socket c2) {
		client1 = c1;
		client2 = c2;
		tempinputoutput = null;
		kartinamasa = new ArrayList<Integer>();
		kartivoraka1 = new ArrayList<Integer>();
		kartivoraka2 = new ArrayList<Integer>();

	}

	public void closeConnection(String client) {
		System.err.println(client + " isklucen");
		try {
			client1.close();
			client2.close();
		} catch (IOException ex) {
		}
	}

	public ArrayList<Integer> randomSpil() {
		// kartite se dvizat po redosledot
		// baklava,detelina,srce,list
		ArrayList<Integer> spil = new ArrayList<Integer>();
		for (int i = 1; i <= 52; i++) {
			spil.add(i);
		}
		Collections.shuffle(spil);
		return spil;
	}

	public void delenjeKarti() {
		for (int i = 0; i < 6; i++) {
			kartivoraka1.add(spil.remove(0));
			kartivoraka2.add(spil.remove(0));
		}
	}

	public void run() {


		ObjectInputStream client1ObjectInputStream = null;
		ObjectOutputStream client1ObjectOutputStream = null;
		ObjectInputStream client2ObjectInputStream = null;
		ObjectOutputStream client2ObjectOutputStream = null;

		// uspesno se konektirani dvete strani
		InputOutputObject input = null;
		InputOutputObject output = null;

		try {
			client1ObjectOutputStream = new ObjectOutputStream(client1.getOutputStream());
			client1ObjectInputStream = new ObjectInputStream(client1.getInputStream());
			client2ObjectOutputStream = new ObjectOutputStream(client2.getOutputStream());
			client2ObjectInputStream = new ObjectInputStream(client2.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			closeConnection("nekoj");
			return;
		}
		spil = randomSpil();
		for (int i = 0; i < 4; i++)
			kartinamasa.add(spil.remove(0));

		// vo igra
		try {
			delenjeKarti();
			output = new InputOutputObject();
			output.setAll(0, 0, 0, 0, kartivoraka1, kartinamasa, -1, null, "ok");
			try {
				client1ObjectOutputStream.writeObject(output);
			} catch (IOException e) {
				output = new InputOutputObject();
				output.setMessage("Disconnected");
				client2ObjectOutputStream.writeObject(output);
				closeConnection("client1");
				return;
			}
			output = new InputOutputObject();
			output.setAll(0, 0, 0, 0, kartivoraka2, kartinamasa, -1, null, "ok1");

			try {
				client2ObjectOutputStream.writeObject(output);
			} catch (IOException e) {
				output = new InputOutputObject();
				output.setMessage("Disconnected");
				client1ObjectOutputStream.writeObject(output);
				closeConnection("client2");
				return;
			}
			// for ciklusot odi do 4 bidejki imame 4
			// delenja na kartite na 2 mesta po 6
			// znaci 2*6 = 12....12*4 = 48...48+4 = 52
			// tie 4 se 4 na masa
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 6; j++) {
					try {
						try {
							input = (InputOutputObject) client1ObjectInputStream.readObject();
						} catch (ClassNotFoundException e) {
						}
						kartivoraka1 = input.getKartivoraka();
						output = new InputOutputObject();
						output.setAll(input.getBrojnapisankiprotivnik(), input.getBrojnapisankijas(),
								input.getZbirnapoeniprotivnik(), input.getZbirnapoenijas(), kartivoraka2,
								input.getKartinamasa(), input.getFrlenakarta(), input.getSobranikarti(), "ok");

					} catch (IOException e) {
						output = new InputOutputObject();
						output.setMessage("Disconnected");
						client2ObjectOutputStream.writeObject(output);
						closeConnection("client1");
						return;
					}
					System.out.println("Primeno od client 1 ");

					try {
						client2ObjectOutputStream.writeObject(output);
					} catch (IOException e) {
						output = new InputOutputObject();
						output.setMessage("Disconnected");
						client1ObjectOutputStream.writeObject(output);
						closeConnection("client2");
						return;
					}

					try {
						try {
							input = (InputOutputObject) client2ObjectInputStream.readObject();
						} catch (ClassNotFoundException e) {
						}
						kartivoraka2 = input.getKartivoraka();

						if (j == 5) {
							delenjeKarti();
							try {//kartite mu gi isprakjame i na vtoriot igrac
								client2ObjectOutputStream.writeObject(kartivoraka2);
								System.out.println("isprateno");
							} catch (Exception e) {
							}
						}
						output = new InputOutputObject();
						output.setAll(input.getBrojnapisankiprotivnik(), input.getBrojnapisankijas(),
								input.getZbirnapoeniprotivnik(), input.getZbirnapoenijas(), kartivoraka1,
								input.getKartinamasa(), input.getFrlenakarta(), input.getSobranikarti(), "ok");
					} catch (Exception e) {
						output = new InputOutputObject();
						output.setMessage("Disconnected");
						client1ObjectOutputStream.writeObject(output);
						closeConnection("client2");
						return;
					}
					System.out.println("Primeno od client 2 ");
					try {
						client1ObjectOutputStream.writeObject(output);
					} catch (IOException e) {
						input.setMessage("Disconnected");
						client2ObjectOutputStream.writeObject(output);
						closeConnection("client1");
						return;
					}

				}
				System.out.println(kartivoraka1.size());
				System.out.println(kartivoraka2.size());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			client1.close();
			client2.close();
		} catch (Exception e) {
		}
	}

}
