package application;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import chess.Color;
import ui.Terminal;
import ui.bash.BashTerminal;
import ui.graphic.GraphicTerminal;
import ui.network.NetworkTerminal;

public class Program {
	private static enum GameType {
		LOCAL(1), HOST(2), CLIENT(3);

		@SuppressWarnings("unused")
		private final int VALUE;

		GameType(int value) {
			this.VALUE = value;
		}

		private static GameType typeByNumber(int value) {
			return switch (value) {
				case 1 -> LOCAL;
				case 2 -> HOST;
				case 3 -> CLIENT;
				default -> throw new IllegalArgumentException("Unexpected value: " + value);
			};
		}

	}

	private static Terminal whitePlayer;
	private static Terminal blackPlayer;

	private static Game game;

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		GameType gameType = GameType.LOCAL;

		/*
		 * -nh : Host -nc : Client
		 */

		if (args.length == 0) {
			try {

				System.out.print("""
						===== ESCOLHA UMA OPÇÃO =====
						1 - Jogo local
						2 - Abrir partida LAN
						3 - Entrar em partida LAN
						  -> """);
				gameType = GameType.typeByNumber(sc.nextInt());
				sc.nextLine();

				if (gameType == GameType.LOCAL) {
					System.out.print("""
							===== ESCOLHA UMA OPÇÃO =====
							b - Jogar aqui
							g - Jogar em interface gráfica
							  -> """);
					char ui = sc.nextLine().charAt(0);
					if (ui != 'b' && ui != 'g')
						throw new IllegalArgumentException("Unexpected value: " + ui);

					System.out.print("Nome do primeiro jogador:\n  -> ");
					String name1 = sc.nextLine();
					System.out.print("Nome do segundo jogador:\n  -> ");
					String name2 = sc.nextLine();

					args = new String[] { "-p1", "-" + ui, name1, "-p2", "-" + ui, name2 };
				} else if (gameType == GameType.HOST) {
					System.out.print("""
							===== ESCOLHA UMA OPÇÃO =====
							b - Jogar aqui
							g - Jogar em interface gráfica
							  -> """);
					char ui = sc.nextLine().charAt(0);
					if (ui != 'b' && ui != 'g')
						throw new IllegalArgumentException("Unexpected value: " + ui);

					System.out.print("Qual o seu nome?\n  -> ");
					String name1 = sc.nextLine();

					args = new String[] { "-nh", "-p1", "-" + ui, name1 };
				} else if (gameType == GameType.CLIENT) {
					System.out.print("Digite o ip e a porta do host (xxx.xxx.xxx.xxx:porta):\n  -> ");
					String socket = sc.nextLine();
					if (!socket.matches("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[:][0-9]{1,6}"))
						throw new IllegalArgumentException("Unexpected value: " + socket);

					System.out.print("""
							===== ESCOLHA UMA OPÇÃO =====
							b - Jogar aqui
							g - Jogar em interface gráfica
							  -> """);
					char ui = sc.nextLine().charAt(0);
					if (ui != 'b' && ui != 'g')
						throw new IllegalArgumentException("Unexpected value: " + ui);

					System.out.print("Qual o seu nome?\n  -> ");
					String name2 = sc.nextLine();

					args = new String[] { "-nc", socket, "-p2", "-" + ui, name2 };
				} else {
					sc.close();
					System.err.println("Tipo de jogo inválido!");
					System.exit(1);
				}

			} catch (Exception e) {
				System.err.println("Inválido!");
				System.exit(0);
			}
		} else if (args[0].contains("-n")) {
			if (args[0].equalsIgnoreCase("-nh")) {
				gameType = GameType.HOST;
			} else if (args[0].equals("-nc")) {
				gameType = GameType.CLIENT;
			}
		}

		if (gameType == GameType.LOCAL) {
			configureLocalGame(args);
		} else if (gameType == GameType.HOST) {
			configureNetworkGameAsHost(args);
		} else if (gameType == GameType.CLIENT) {
			configureNetworkGameAsClient(args);
		}
		try {
			sc.close();
		} catch (Exception e) {
		}
	}

	private static void configureNetworkGameAsClient(String[] args) {
		try {
			Terminal localTerminal = null;
			String socket = args[1];
			String name = null;
			if (!args[2].equalsIgnoreCase("-p2")) {
				System.out.println("-nc ip:porta -p2 (-b or -g) name");
				System.exit(1);
			} else {
				name = args[4];
				if (args[3].equalsIgnoreCase("-b")) {
					localTerminal = new BashTerminal(Color.BLACK, name);
				} else if (args[3].equalsIgnoreCase("-g")) {
					localTerminal = new GraphicTerminal(Color.BLACK, name);
				} else {
					System.out.println("-nc ip:porta -p2 (-b or -g) name");
					System.exit(1);
				}
			}
			Socket client = new Socket(socket.split(":")[0], Integer.parseInt(socket.split(":")[1]));

			Program.game = new ClientGame(localTerminal, client);
		} catch (ConnectException e) {
			System.out.println("Servidor não encontrado");
			System.exit(4);
		} catch (IOException e) {
			System.out.println("Program.configureNetworkGameAsClient()");
			e.printStackTrace();
		} catch (IndexOutOfBoundsException | NullPointerException e) {
			System.out.println("-nc ip:porta -p2 (-b or -g) name");
			System.exit(1);
		}
	}

	private static void configureNetworkGameAsHost(String[] args) {
		if (args[1].equalsIgnoreCase("-p1") && args[2].equalsIgnoreCase("-b")) {
			Program.whitePlayer = new BashTerminal(Color.WHITE, args[3]);
		} else if (args[2].equalsIgnoreCase("-g")) {
			Program.whitePlayer = new GraphicTerminal(Color.WHITE, args[3]);
		} else {
			System.out.println("-p1 (-b or -g) namePlayer1");
			System.exit(1);
		}
		try {
			ServerSocket host = new ServerSocket(0);

			whitePlayer.message("Servidor %s:%d esperando cliente"
					.formatted(InetAddress.getLocalHost().getHostAddress(), host.getLocalPort()));

			Socket client = host.accept();
			System.out.println("Cliente conectado: " + client.getInetAddress().getHostAddress());

			String clientName = "jubiscreia";

			whitePlayer.message("Nome do cliente: " + clientName);
			Program.blackPlayer = new NetworkTerminal(Color.BLACK, clientName, host, client);

		} catch (IOException e) {
			whitePlayer.message("Deu problema com a conexão com o cliente");
			e.printStackTrace();
			System.exit(2);
		}

		Program.game = new HostGame(whitePlayer, blackPlayer);
	}

	private static void configureLocalGame(String[] args) {
		try {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-p1")) {
					System.out.println("configurando p1");
					i++;
					if (args[i].equalsIgnoreCase("-b")) {
						i++;
						Program.whitePlayer = new BashTerminal(Color.WHITE, args[i]);
					} else if (args[i].equalsIgnoreCase("-g")) {
						i++;
						Program.whitePlayer = new GraphicTerminal(Color.WHITE, args[i]);
					} else {
						System.out.println("-p1 (-b or -g) namePlayer1 -p2 (-b or -g) namePlayer2");
					}
				} else if (args[i].equalsIgnoreCase("-p2")) {
					System.out.println("configurando p2");
					i++;
					if (args[i].equalsIgnoreCase("-b")) {
						i++;
						Program.blackPlayer = new BashTerminal(Color.BLACK, args[i]);
					} else if (args[i].equalsIgnoreCase("-g")) {
						i++;
						Program.blackPlayer = new GraphicTerminal(Color.BLACK, args[i]);
					} else {
						System.out.println("-p1 (-b or -g) namePlayer1 -p2 (-b or -g) namePlayer2");
						System.exit(1);
					}
				} else {
					System.out.println("Confira os argumentos: " + Arrays.toString(args));
					System.exit(1);
				}
			}
		} catch (NullPointerException e) {
			System.out.println("-p1 (-b or -g) namePlayer1 -p2 (-b or -g) namePlayer2");
			System.exit(1);
		}

		Program.game = new LocalGame(whitePlayer, whitePlayer.getName(), blackPlayer.getName());
	}

	public static String chosePieceTypeToPromotion() {
		return game.chosePieceTypeToPromotion();
	}

}
