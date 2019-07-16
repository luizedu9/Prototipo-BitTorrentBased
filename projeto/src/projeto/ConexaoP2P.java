package projeto;

import java.net.*;
import java.util.concurrent.Semaphore;
import java.io.*;

public class ConexaoP2P {

	public ClienteTCP cliente;
	public ServidorTCP servidor;

	public int PORT_SERVER = 19470;

	public boolean clienteTerminou = false;
	public boolean servidorTerminou = false;

	public String diretorioEntrada;
	public String diretorioMetadado;
	Metadado metadado;
	Semaphore semaforo = new Semaphore(1);

	public ConexaoP2P(String tipo, String dir1, String dir2) {
		diretorioEntrada = new String(dir1);
		diretorioMetadado = new String(dir2);
		try {
			if (tipo.equals("seeder")) {
				servidor = new ServidorTCP();
				System.out.println("Seeder Criado com Sucesso!");
			} else {
				cliente = new ClienteTCP();
				System.out.println("Leecher Criado com Sucesso!");
			}
		} catch (Exception e) {
			System.err.println("ERRO: " + e.getMessage());
			System.exit(-1);
		}
	}

	public void iniciaPar() {
		Metadado.carregarCabecalhoMetadado(diretorioMetadado); // Inicia Metadado;
		new Thread(servidor).start();
		new Thread(cliente).start();
		while (!clienteTerminou && !servidorTerminou) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			};
		}
		System.exit(0);
	}

	class ClienteTCP implements Runnable {
		byte[] peca = new byte[Metadado.tamanhoPeca];
		Socket conexao;

		public boolean verificaIntegridade(String recebido, String original) {
			if (recebido.equals(original)) {
				return (true);
			} else {
				System.out.println("Não Integro");
				return (false);
			}
		}

		private String getVizinho() {
			return "192.168.0.11";
		}

		public void run() {
			solicitaServico();
		}

		public void solicitaServico() {
			int i, j, k, peca;
			String sha1Metadado, sha1Recebido;
			DataOutputStream saida;
			DataInputStream entrada;
			RandomAccessFile arquivo = null;
			try {
				arquivo = new RandomAccessFile(new File(diretorioEntrada), "rw");
				arquivo = criarArquivoSaida();
				byte[] bytes = new byte[Metadado.tamanhoPeca];
				byte[] byteUltimo = new byte[Metadado.tamPecaFinal];
				conexao = new Socket(getVizinho(), PORT_SERVER);
				saida = new DataOutputStream(conexao.getOutputStream());
				entrada = new DataInputStream(conexao.getInputStream());
				boolean[] historico = new boolean[(int) Metadado.qtdPeca];
				for (i = 0; i < Metadado.qtdPeca; i++) {
					historico[i] = false;
				}

				// SELEÇÃO DE PEÇAS
				for (i = 0; i < Metadado.qtdPeca; i++) { // Recebe todas as peças;
					k = (int) (Math.random() * (Metadado.qtdPeca + 1 - i));
					peca = 0;
					j = 0;
					if (k == 0) {
						while (historico[peca] == true) {
							peca++;
						}
					} else {
						do {
							if (historico[peca] == false) {
								j++;
							}
							peca++;
						} while (j < k);
						peca--;
					}
					do {
						saida.writeLong((long) peca);
						System.out.println("Peca requerida: " + peca);
						if (peca == Metadado.qtdPeca - 1) { // Ultima peça;
							entrada.readFully(byteUltimo);
							System.out.println("Peca recebida" + peca);
							System.out.println();
							sha1Metadado = Metadado.carregarPecaMetadado(diretorioMetadado, peca);
							sha1Recebido = new String(Metadado.sha1(byteUltimo).getBytes());
							if (verificaIntegridade(sha1Metadado, sha1Recebido)) {
								historico[peca] = true;
								semaforo.acquire();
								montarArquivo(arquivo, peca, byteUltimo);
								semaforo.release();
							}
						} else {
							entrada.readFully(bytes);
							System.out.println("Peca recebida" + peca);
							sha1Metadado = Metadado.carregarPecaMetadado(diretorioMetadado, peca);
							sha1Recebido = new String(Metadado.sha1(bytes).getBytes());
							if (verificaIntegridade(sha1Metadado, sha1Recebido)) {
								historico[peca] = true;
								semaforo.acquire();
								montarArquivo(arquivo, peca, bytes);
								semaforo.release();
							}
						}
					} while (historico[peca] == false);
				}
				saida.writeLong(-1);
				saida.close();
				entrada.close();
				conexao.close();
				clienteTerminou = true;
			} catch (Exception e) {
				System.err.println("ERRO: " + e.getMessage());
			} finally {
				try {
					arquivo.close();
				} catch (Exception e) {
					System.err.println("ERRO: " + e.getMessage());
				}
			}
		}

		public RandomAccessFile criarArquivoSaida() throws IOException { // Aloca o arquivo inteiro do tamanho original com zeros;
			int i;
			byte[] bytes = new byte[Metadado.tamanhoPeca];
			byte[] byteUltimo = new byte[Metadado.tamPecaFinal];
			for (i = 0; i < Metadado.tamanhoPeca; i++) {
				bytes[i] = 0;
			}
			for (i = 0; i < Metadado.tamPecaFinal; i++) {
				byteUltimo[i] = 0;
			}
			RandomAccessFile arquivo = new RandomAccessFile(new File(diretorioEntrada), "rw");
			try {
				for (i = 0; i < Metadado.qtdPeca - 1; i++) {
					arquivo.seek(i * Metadado.tamanhoPeca);
					arquivo.write(bytes);
				}
				arquivo.seek((Metadado.qtdPeca - 1) * Metadado.tamanhoPeca);
				arquivo.write(byteUltimo);
			} catch (FileNotFoundException e) {
				System.err.println("ERRO: " + e.getMessage());
			} catch (IOException e) {
				System.err.println("ERRO: " + e.getMessage());
			}
			return (arquivo);
		}

		public void montarArquivo(RandomAccessFile arquivo, long requisicao, byte[] bytes) throws IOException {
			long pecaAtual = (requisicao) * Metadado.tamanhoPeca;
			long ultimaPeca = Metadado.qtdPeca - 1;
			byte[] byteUltimo = new byte[Metadado.tamPecaFinal];
			try {
				if (requisicao != ultimaPeca) {
					arquivo.seek(pecaAtual);
					arquivo.write(bytes);
				} else {
					arquivo.seek(pecaAtual);
					arquivo.write(bytes);
				}
			} catch (IOException e) {
				System.err.println("ERRO: " + e.getMessage());
			} catch (Exception e) {
				System.err.println("ERRO: " + e.getMessage());
			}
		}
	}

	class ServidorTCP implements Runnable {
		ServerSocket serverSocket;
		Socket conexao;
		DataOutputStream saida;
		DataInputStream entrada;

		public void run() {
			try {
				serverSocket = new ServerSocket(PORT_SERVER);
				proveServico();
			} catch (Exception e) {
				System.err.println("ERRO: " + e.getMessage());
			}
		}

		public void proveServico() {
			try {
				conexao = serverSocket.accept();
				saida = new DataOutputStream(conexao.getOutputStream());
				entrada = new DataInputStream(conexao.getInputStream());
				boolean[] historico = new boolean[(int) Metadado.qtdPeca];
				int j;
				byte[] bytes = new byte[Metadado.tamanhoPeca];
				byte[] byteUltimo = new byte[Metadado.tamPecaFinal];
				long requisicao;
				for (j = 0; j < Metadado.qtdPeca; j++) {
					historico[j] = true;
				}
				while (true) {
					requisicao = entrada.readLong();
					System.out.println("Recebi a requisicao da peca" + requisicao);
					if (requisicao == -1) {
						servidorTerminou = true;
					} else {
						if (historico[(int) requisicao] = true) {
							if (requisicao != (Metadado.qtdPeca - 1)) {
								semaforo.acquire();
								bytes = lerArquivo(requisicao);
								semaforo.release();
								saida.write(bytes);
								System.out.println("Enviei a peca" + requisicao);
							} else {
								semaforo.acquire();
								byteUltimo = lerArquivo(requisicao);
								semaforo.release();
								saida.write(byteUltimo);
								System.out.println("Enviei a peca" + requisicao);
							}
						}
					}
				}
			} catch (IOException e) {
				System.err.println("ERRO: " + e.getMessage());
			} catch (InterruptedException e) {
				System.err.println("ERRO: " + e.getMessage());
			} finally {
				try {
					saida.close();
					entrada.close();
					conexao.close();
					servidorTerminou = true;
					serverSocket.close();
				} catch (Exception e) {
					System.err.println("ERRO: " + e.getMessage());
				}
			}
		}
	}

	public byte[] lerArquivo(long requisicao) throws IOException {
		long pecaAtual = (requisicao) * Metadado.tamanhoPeca;
		long ultimaPeca = Metadado.qtdPeca - 1;
		byte[] bytes = new byte[Metadado.tamanhoPeca];
		byte[] byteUltimo = new byte[Metadado.tamPecaFinal];
		RandomAccessFile arquivo = new RandomAccessFile(new File(diretorioEntrada), "rw");
		try {
			if (requisicao != ultimaPeca) {
				arquivo.seek(pecaAtual);
				arquivo.readFully(bytes);
			} else {
				arquivo.seek(pecaAtual);
				arquivo.readFully(byteUltimo);
				return (byteUltimo);
			}
		} catch (IOException e) {
			System.err.println("ERRO: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("ERRO: " + e.getCause());
		} finally {
			arquivo.close();
		}
		return (bytes);
	}
}