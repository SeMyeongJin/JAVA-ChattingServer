/*
 * 한명의 클라이언트와 통신하기 위한 기능을 모아놓은 클래스
 */

package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteOrder;

import MsgPacker.*;

public class Client {
	
	Socket socket;
	Client cli;
	private GameRoom room;
	int roomNum;
	
	MessagePacker msg;
	
	public Client(Socket sock) 
	{
		this.socket = sock;   
		cli = this;
		receive();
	}
	
	//클라이언로부터 메시지를 전달받는 메소드
	public void receive()//()
	{
		// 하나의 스레드 만들 때 Runnable객체 많이 이용
		Runnable thread = new Runnable() 
		{
			// Runnable 안엔 run함수 있어야함 run함수엔 하나의 스레드가 어떤 모듈로서 동작할것인지 정의
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[1024];
						int length = in.read(buffer);
						while(length == -1) throw new IOException();
						System.out.println("[메시지 수신 성공]" 
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName());

						msg = new MessagePacker(buffer);
						byte protocol = msg.getProtocol();
						String chatStr;
	
						switch(protocol)
						{
						case MessageProtocol.LOGIN:{
							String name = DBManager.login(msg.getString(), msg.getString());
							
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.LOGIN_SUCC);
							reMsg.add(name);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.SIGNUP:{
							if (DBManager.signup(msg.getString(), msg.getString(), msg.getString()))
							{
								MessagePacker reMsg = new MessagePacker();
								reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
								reMsg.SetProtocol(MessageProtocol.SIGNUP_SUCC);
								byte[] message = reMsg.Finish();
								send(message);
								break;
							}
						}
						case MessageProtocol.ROOM1:{
							RoomManager.rooms.get(0).enterRoom(cli,0);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM2:{
							RoomManager.rooms.get(1).enterRoom(cli,1);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM3:{
							RoomManager.rooms.get(2).enterRoom(cli,2);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM4:{
							RoomManager.rooms.get(3).enterRoom(cli,3);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM5:{
							RoomManager.rooms.get(4).enterRoom(cli,4);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM6:{
							RoomManager.rooms.get(5).enterRoom(cli,5);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM7:{
							RoomManager.rooms.get(6).enterRoom(cli,6);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.ROOM8:{
							RoomManager.rooms.get(7).enterRoom(cli,7);
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.ROOMENTER);
							byte[] message = reMsg.Finish();
							send(message);
							break;
						 }
						case MessageProtocol.CHAT:{
							roomNum = room.getRoomNum();
							chatStr = msg.getString();
							MessagePacker reMsg = new MessagePacker();
							reMsg.SetEndianType(ByteOrder.BIG_ENDIAN);
							reMsg.SetProtocol(MessageProtocol.CHAT);
							reMsg.add(chatStr);
							byte[] message = reMsg.Finish();
							for(Client client : RoomManager.rooms.get(roomNum).getUserList())
							{
								client.send(message);
							}
							break;
						 }
						}
					}
				} 
				catch(Exception e) 
				{
					try 
					{
						System.out.println("[메시지 수신 오류]"
						+ socket.getRemoteSocketAddress()
						+ ": " + Thread.currentThread().getName() + e);
					}
					catch(Exception e2) 
					{
					   e.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);   // 스레드풀에 만들어진 스레드 등록
	}
	
	//클라이언트에게 메시지를 전송하는 메소드
	public void send(byte[] message)
	{
		Runnable thread = new Runnable() 
		{
			@Override
			public void run() 
			{
				try 
				{
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message;
					out.write(buffer);
					out.flush();
				} 
				catch(Exception e) 
				{
					try 
					{
						System.out.println("[메시지 송신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}
					catch(Exception e2) 
					{
						   e.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}

    
    public void enterRoom(GameRoom room) 
    {
      this.room = room;
    }

    public void exitRoom()
    {
        this.room = null;
    }

    public GameRoom getRoom() 
    {
        return room;
    }

    public void setRoom(GameRoom room) 
    {
        this.room = room;
    }

    public Socket getSock() 
    {
        return socket;
    }

    public void setSock(Socket sock) 
    {
        this.socket = sock;
    }
    
    public int getRoomNum() 
    {
        return roomNum;
    }

    public void setRoomNum(int Num) 
    {
        this.roomNum = Num;
    }
}
