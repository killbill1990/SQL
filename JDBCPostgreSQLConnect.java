package postgresql;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

public class JDBCPostgreSQLConnect {
	
	Connection connection; 
		
	//private final String url = "jdbc:postgresql://localhost:5433/Final";
	//private final String user = "postgres";
	//private final String password = "19991924";
	
	public void connect(String ip, String dbName, String username, String password) {
		try {
			
			
			connection = DriverManager.getConnection("jdbc:postgresql://"+ip+":5433/"+dbName, username, password);
			
			if(connection != null) {
				System.out.println("Connect successfully!!!");
				
			}else {
				System.out.println("Failed!!!");
			}
			
			Statement statement =  connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT VERSION()");
			if(resultSet.next()) {
				System.out.println(resultSet.getString(1));
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String selectHotel(String prename) {
		try {
			
			Scanner sc = new Scanner(System.in);
			Vector<String> table = new Vector<String>();
			
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT name FROM \"hotel\" WHERE left(\"name\",1) = '"+prename+"'");
			int i = 1;
			
			while (rs.next()) {
				System.out.println(i+") "+rs.getString("name"));
				table.add(rs.getString("name"));
				i++;
			}
			
			System.out.println("Enter the number of the hotel: ");
			String str = sc.nextLine();
			
			System.out.println("You select: "+table.get(Integer.parseInt(str)-1));
			
			rs.close();
			return table.get(Integer.parseInt(str)-1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public int printClients(String hotelName,String prename) {
		try {
			Scanner sc = new Scanner(System.in);
			int hotelID = 0;
			Vector<Integer> roomID = new Vector<Integer>();
			Vector<Integer> clients = new Vector<Integer>();
			Vector<Integer> personID = new Vector<Integer>();
			
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM \"hotel\" WHERE \"name\" = '"+hotelName+"'");
			
			while (rs.next()) {
				hotelID = rs.getInt("idHotel");
			}
			
			rs.close();
			
			if(hotelID > 0)
				rs = st.executeQuery("SELECT * FROM \"room\" WHERE \"idHotel\" = "+hotelID);
			
			while (rs.next()) {
				roomID.add(rs.getInt("idRoom"));
			}
			
			rs.close();
			
			rs = st.executeQuery("SELECT * FROM \"roombooking\" ORDER BY \"roomID\"");
			
			while (rs.next()) {
				if(roomID.contains(rs.getInt("roomID"))) {
					clients.add(rs.getInt("bookedforpersonID"));
				}
			}
			
			rs.close();
			
			rs = st.executeQuery("SELECT * FROM \"person\" WHERE left(\"lname\",1) = '"+prename+ "' ORDER BY lname");
			
			int i = 1;
			while (rs.next()) {
				if(clients.contains(rs.getInt("idPerson"))) {
					System.out.println(i+") Person ID: "+rs.getInt("idPerson")+" | First Name: "+rs.getString("fname")+" | Last Name: "+rs.getString("lname")+" | Sex: "+rs.getString("sex")+" | Date of Birth: "+rs.getDate("dateofbirth")+" | Address: "+rs.getString("address")+" | City: "+rs.getString("city")+" | Country: "+rs.getString("country"));
					personID.add(rs.getInt("idPerson"));
					i++;
				}
			}
			
			System.out.println("Enter the number of the person: ");
			String str = sc.nextLine();
			
			rs.close();
			return personID.get(Integer.parseInt(str)-1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
		
	}
	
	
	public void changes(int personID) {
		
		Statement st;
		int i = 1;
		Scanner sc = new Scanner(System.in);
		Vector<Integer> hotelbookingID = new Vector<Integer>();
		Vector<Integer> roomID = new Vector<Integer>();
		
		try {
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("select \"hotelbookingID\", \"roomID\", checkin, checkout, rate from roombooking where \"bookedforpersonID\" = "+ personID + "order by \"hotelbookingID\"");
		
			while(rs.next()) {
				System.out.println(i+") HotelBookingID: "+rs.getInt("hotelbookingID")+" | RoomID: "+rs.getInt("roomID")+" | Check_in: "+rs.getDate("checkin")+" | Check_out: "+rs.getDate("checkout")+" | Rate: "+rs.getInt("rate"));
				hotelbookingID.add(rs.getInt("hotelbookingID"));
				roomID.add(rs.getInt("roomID"));
				i++;
			}
			
			rs.close();
			
			System.out.println("Enter the number of the reservation: ");
			String str = sc.nextLine();
			

			
			if (Integer.parseInt(str) != 0) {
				
				System.out.println("1) Change checkout");
				System.out.println("2) Change rate");
				
				String choice = sc.nextLine();
				
				if (Integer.parseInt(choice) == 1) {
					System.out.println("Enter the the new checkout: ");
					String out = sc.nextLine();
					
					st.executeUpdate("update roombooking set checkout = '"+ out +"' where \"hotelbookingID\" ="+hotelbookingID.get(Integer.parseInt(str)-1));
				}
				else if (Integer.parseInt(choice) == 2) {
					System.out.println("Enter the the new rate: ");
					String rate = sc.nextLine();
					int hotelID = 0;
					int newRate = 0;
					
					rs = st.executeQuery("SELECT \"idHotel\" FROM room WHERE \"idRoom\" = "+roomID.get(Integer.parseInt(str)-1));
					while(rs.next()) {
						hotelID = rs.getInt(1);
					}
					
					rs.close();
					
					
					st.executeUpdate("UPDATE roomrate SET rate = "+rate+" WHERE id_hotel = "+hotelID+" and roomtype = (select rm.roomtype from room rm where \"idRoom\" = "+roomID.get(Integer.parseInt(str)-1)+")");
					

					
					rs = st.executeQuery("SELECT (rate-rate*discount*0.01) FROM roomrate WHERE roomtype = (select rm.roomtype from room rm where \"idRoom\" = "+roomID.get(Integer.parseInt(str)-1)+" and id_hotel = "+hotelID+")");

					
					while(rs.next()) {
						newRate = rs.getInt(1);
					}
					
					st.executeUpdate("UPDATE roombooking set rate = "+newRate+" where \"roomID\"= "+roomID.get(Integer.parseInt(str)-1));
					
					st.executeUpdate("UPDATE hotelbooking SET totalamount = (SELECT total ("+hotelbookingID.get(Integer.parseInt(str)-1)+"))");
	
				}
				
			}
			else
				return;
			
			//rs.close();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	
	public void avaliableRoom(String hotelName) {
		Scanner sc = new Scanner(System.in);
		int hotelID = 0;
		Vector<Integer> roomSelect = new Vector<Integer>();
		Vector<String> roomType = new Vector<String>();
		int roomID = 0;
		int clientID = 0;
		int rate = 0;
		int bookedID = 0;
		
		System.out.println("Enter checkin: ");
		String checkin = sc.nextLine();
		
		System.out.println("Enter checkout: ");
		String checkout = sc.nextLine();
		
		Statement st;
		try {
			
			st = connection.createStatement();
			ResultSet rs = st.executeQuery("SELECT * FROM \"hotel\" WHERE \"name\" = '"+hotelName+"'");
			
			while (rs.next()) {
				hotelID = rs.getInt("idHotel");
			}
			
			rs.close();
			
			rs = st.executeQuery("select distinct \"roomID\", number, roomtype  from room join roombooking on (\"roomID\" = \"idRoom\") where \"idHotel\" = "+hotelID+" and \"roomID\" not in (select \"roomID\" from room join roombooking on (\"roomID\" = \"idRoom\") where \"idHotel\" = "+hotelID+" and ('"+checkout+"' > checkin) AND ('"+checkin+"' < checkout)) order by \"roomID\"");
			
			int i =1;
			while(rs.next()) {
				System.out.println(i+") Room ID: "+rs.getInt("roomID")+" | number: "+rs.getInt("number")+" | Room Type: " + rs.getString("roomtype"));
				roomSelect.add(rs.getInt("roomID"));
				roomType.add(rs.getString("roomtype"));
				i++;
			}
			
			rs.close();
			
			System.out.println("Enter the number of the room or press 0: ");
			String choice = sc.nextLine();
			
			String type = roomType.get(Integer.parseInt(choice)-1);
			
			if(Integer.parseInt(choice) != 0) {
				roomID = roomSelect.get(Integer.parseInt(choice)-1);
			
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate localDate = LocalDate.parse(checkin, formatter);
			LocalDate date = localDate.minusDays(10);
			String formattedString = date.format(formatter);
			
			System.out.println("Enter the document: ");
			String document = sc.nextLine();
			
			rs = st.executeQuery("SELECT * FROM client WHERE documentclient = '"+document+"'");
			
			
			System.out.println(document);
			
			while(rs.next()) {
				clientID = rs.getInt("idClient");
			}
			
			rs.close();
			
			rs = st.executeQuery("SELECT (rate-rate*discount*0.01) FROM roomrate WHERE roomtype = '"+type+"' and \"id_hotel\" = "+hotelID);
			
			while(rs.next()) {
				rate = rs.getInt(1);
				System.out.println(rate);
			}
			
			PreparedStatement stmnt = connection.prepareStatement("INSERT INTO hotelbooking (reservationdate,cancellationdate,totalamount,\"bookedbyclientID\",payed,paymethod,status) VALUES(?,?,?,?,?, ?::payment_method,?::booking_status) ");
	        stmnt.setDate(1, java.sql.Date.valueOf("2021-05-21"));
	        stmnt.setDate(2, java.sql.Date.valueOf(formattedString));
	        stmnt.setInt(3, 0);
	        stmnt.setInt(4,clientID);
	        stmnt.setBoolean(5, false);
	        stmnt.setString(6,"cash".toString());
	        stmnt.setString(7, "pending".toString());
	        stmnt.executeUpdate();
	        stmnt.close();
			
			rs = st.executeQuery("SELECT max(idhotelbooking) FROM hotelbooking");
			
			while(rs.next()) {
				bookedID = rs.getInt(1);
			}
			
			rs.close();
			
			
			stmnt = connection.prepareStatement("INSERT INTO roombooking (\"hotelbookingID\",\"roomID\",\"bookedforpersonID\",checkin,checkout,rate) VALUES(?,?,?,?,?,?) ");
			
	        stmnt.setInt(1, bookedID);
	        stmnt.setInt(2, roomID);
	        stmnt.setInt(3, clientID);
	        stmnt.setDate(4, java.sql.Date.valueOf(checkin));
	        stmnt.setDate(5, java.sql.Date.valueOf(checkout));
	        stmnt.setInt(6, rate);
	        stmnt.executeUpdate();
	        stmnt.close();
	        
	        st.executeUpdate("UPDATE hotelbooking SET totalamount = (SELECT total("+bookedID+")) WHERE idhotelbooking = '"+bookedID+"'");
	        
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public void menu(JDBCPostgreSQLConnect sqlConnect) {
		
		Scanner sc = new Scanner(System.in);
		String choice;
		String in;
		
		System.out.println("1) Enter in databese");
		System.out.println("2) Search in spesific Hotel");
		System.out.println("3) For exit press 0");
		
		choice = sc.nextLine();
		
		while(Integer.parseInt(choice) != 0) {
			
			if (Integer.parseInt(choice) == 1) {
				System.out.println("Enter the ip: ");
				String ip = sc.nextLine();
				System.out.println("Enter the DB name: ");
				String dbName = sc.nextLine();
				System.out.println("Enter the user name: ");
				String username = sc.nextLine();
				System.out.println("Enter the password: ");
				String password = sc.nextLine();
				sqlConnect.connect(ip,dbName,username,password);
			} 
			
			else if (Integer.parseInt(choice) == 2) {
				System.out.println("Enter the first letter of the hotel: ");
				String prename = sc.nextLine();
				String hotelName = sqlConnect.selectHotel(prename);
				
				System.out.println("1) Search clients");
				System.out.println("2) Search for reservation");
				System.out.println("3) Print avalaible room");	
				in = sc.nextLine();
				
				if(Integer.parseInt(in) == 1) {
					System.out.println("Enter the first letter of the clients: ");
					String clientName = sc.nextLine();
					int personID = sqlConnect.printClients(hotelName, clientName);					
				}
				else if (Integer.parseInt(in) == 2) {
					System.out.println("Enter the first letter of the clients: ");
					String clientName = sc.nextLine();
					int personID = sqlConnect.printClients(hotelName, clientName);					
					sqlConnect.changes(personID);
				}
				else if (Integer.parseInt(in) == 3) {
					sqlConnect.avaliableRoom(hotelName);
				}
				
			}
			
			else if ( (Integer.parseInt(choice) != 1) &&  (Integer.parseInt(choice) != 2) ) {
				System.out.println("Give right argument!!!!");
			}
			
			System.out.println("1) Enter in databese");
			System.out.println("2) Search in spesific Hotel");
			System.out.println("3) For exit press 0");
			choice = sc.nextLine();
		}
		
		
	}
	
	public static void main(String[] args) {
		JDBCPostgreSQLConnect sqlConnect = new JDBCPostgreSQLConnect();
		sqlConnect.menu(sqlConnect);
		System.out.println("END!!!");
	}

}
