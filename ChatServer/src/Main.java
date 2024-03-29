
package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
    // ExecutorService는 여러개의 스레드를 효율적으로 관리하게 해주는 대표적인 라이브러리. 한정된 자원으로 안정적으로 서버를 사용하기 위해 스레드풀 사용
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	TextArea textArea;
	Client cli;

	// 서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드
	public void startServer(String IP,int port)
	{
		RoomManager.createRoom();
		DBManager.initDB();
		
		try 
		{
			serverSocket = new ServerSocket();    // 서버에서 클라 받을 소켓 생성
			serverSocket.bind(new InetSocketAddress(IP,port));   // 주소 bind
		} 
		catch(Exception e) 
		{
			e.printStackTrace();
			if(!serverSocket.isClosed()) 
			{
				stopServer();
			}
			return;
		}
		
		//클라이언트가 접속할 때까지 계속 기다리는 스레드
		Runnable thread = new Runnable() 
		{
			@Override
			public void run() 
			{ 
				while(true) 
				{
					try 
					{
						Socket socket = serverSocket.accept();
						cli = new Client(socket);
						clients.add(cli);   //클라이언트 배열에 새롭게 접속한 클라이언트 추가시킴
					 
						Platform.runLater(() -> {
		                     String message = String.format("%s 가 입장했습니다.\n", socket.getRemoteSocketAddress().toString());
		                     textArea.appendText(message);
		                  });

						System.out.println("[클라이언트 접속]" + socket.getRemoteSocketAddress()+ ": " + Thread.currentThread().getName());
					} 
					catch(Exception e) 
					{
						e.printStackTrace();
						if(!serverSocket.isClosed()) 
						{
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();  //스레드풀 초기화
		threadPool.submit(thread);
	}

	// 서버의 작동을 중지시키는 메소드
	public void stopServer()
	{
		try 
		{
			//현재 작동 중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) 
			{
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// 서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) 
			{
				serverSocket.close();
			}
			//쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) 
			{
				threadPool.isShutdown();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	// UI를 생성하고, 실질적으로 프로그램을 동작시키는 메소드
	@Override
	public void start(Stage primaryStage) 
	{
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
	    textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("End Ran Chat Server");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9000;
		
		startServer(IP,port);
		Platform.runLater(()->{     
			String message = String.format("Start ran chat server\n",IP,port);
			textArea.appendText(message);
		});
		
		toggleButton.setOnAction(event->{
			if(toggleButton.getText().equals("End Ran Chat Server")) 
			{
				stopServer();
				Platform.runLater(()->{     //javafx는 버튼을 눌렀을때 바로 텍스트를 쓰게 하면 안됨-> runLAter함수 사용
					String message = String.format("End ran chat server\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("Start Ran Chat Server");   //토글버튼을 종료하기로 바꿈
				});
			} 
			else 
			{
				startServer(IP,port);
				Platform.runLater(()->{     //javafx는 버튼을 눌렀을때 바로 텍스트를 쓰게 하면 안됨-> runLAter함수 사용
					String message = String.format("Start ran chat server\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("End ran chat server\n");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("Ran Chat Server");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// 프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}