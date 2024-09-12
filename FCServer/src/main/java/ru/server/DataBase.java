package server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.sqlite.SQLiteException;

import door.Message.MessageDTO;
import fox.adds.Out;
import gui.MonitorFrame;


public class DataBase {
	private static Icon messageIcon;

	private static String dbName = "db";
	private static Connection conn;
	
	
	public static void loadDataBase() {
		try {
			connection();
			checkDB();
		} catch (ClassNotFoundException | SQLException e) {e.printStackTrace();}
	}
	
	// --------ПОДКЛЮЧЕНИЕ К БАЗЕ ДАННЫХ--------
	static void connection() throws ClassNotFoundException, SQLException {
		if (conn != null) {conn.close();}
		
	    Class.forName("org.sqlite.JDBC");
	    File dbDir = new File("./data/");
	    if (!dbDir.exists()) {dbDir.mkdir();}
	    conn = DriverManager.getConnection("jdbc:sqlite:data\\" + dbName + ".db");
	    Out.Print(DataBase.class, 1, "База '" + dbName + "' Подключена!");
	}
	
	// --------Создание таблицы--------
	static void checkDB() throws ClassNotFoundException, SQLException {
		try (Statement statmt = conn.createStatement()) {
	//		PRAGMA foreign_keys = 0;
			
			statmt.execute("CREATE TABLE if not exists 'clients' (" + 
					" 'id' INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					" 'uid' STRING (32) UNIQUE NOT NULL, " + 
					" 'name' STRING (32) UNIQUE NOT NULL, " + 
					" 'password' STRING (32) NOT NULL, " + 
//					+ "'description' TEXT, "
//					+ "'modificКРС' DOUBLE NOT NULL DEFAULT (0), "
//					+ "'modificМРС' DOUBLE NOT NULL DEFAULT (0), "
//					+ "'modificHrs' DOUBLE NOT NULL DEFAULT (0), "
//					+ "'picpath' STRING (64)"
					" 'last_enter' STRING (32));");

//			statmt.execute("CREATE TABLE if not exists 'client_passwords' ("
//					+ "'id' INTEGER PRIMARY KEY AUTOINCREMENT, "
//					+ "'password' STRING (128));");
			
	//		PRAGMA foreign_keys = 1;
			Out.Print(DataBase.class, 0, "Таблицы созданы или уже существуют.");
		} catch (SQLException e) {e.printStackTrace();}
	}
	
	
	// -------- Добавление клиента --------
	public synchronized static boolean addClient(MessageDTO incomeDTO) {
		// проверить, нет ли еще такого пользователя в базе, иначе создаём нового.
		if (incomeDTO.getFrom() == null || incomeDTO.getFrom().isBlank() 
				|| incomeDTO.getBody() == null || incomeDTO.getBody().isBlank()
				|| incomeDTO.getPassword() == null || incomeDTO.getPassword().isBlank()) {
			MonitorFrame.toConsole("Регистрация нового клиента невозможна! Имя, пароль и UID: '" + incomeDTO.getFrom() + "', '" + incomeDTO.getPassword() + "', '" + incomeDTO.getBody() + "'.");		
			return false;
		}
		
		MonitorFrame.toConsole("Регистрация нового клиента '" + incomeDTO.getFrom() + "'.");				
		try (Statement statmt = conn.createStatement()) {
//			try (ResultSet rs = statmt.executeQuery("SELECT * FROM clients WHERE clients.name = " + incomeDTO.getFrom() + ";")) {
//				while (rs.next()) {
					statmt.execute("INSERT INTO 'clients' ('uid', 'name', 'password', 'last_enter'"
							+ ") VALUES ('"
							+ incomeDTO.getBody() + "','" + incomeDTO.getFrom() + "','" + incomeDTO.getPassword() + "','" + System.currentTimeMillis() + "');");
					return true;
//				}
//			} catch (SQLException e) {e.printStackTrace();}
		} catch (SQLiteException e) {
			MonitorFrame.toConsole("Регистрация нового клиента '" + incomeDTO.getFrom() + "': " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			MonitorFrame.toConsole("Регистрация нового клиента '" + incomeDTO.getFrom() + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	void showErrMessage() {
		System.out.println("DataBase.showErrMessage(): Не все поля были заполнены. Отбой.");
		JOptionPane.showMessageDialog(null, 
				"<html><b><h2 color='RED'>Операция отменена!</h2></b><br>Не достаточно данных для создания<br>нового элемента.", 
				 "Внимание!", JOptionPane.ERROR_MESSAGE, messageIcon);
	}

	// -------- Cуществует ли в базе данных клиент с таким UID --------
	public synchronized static boolean containsUID(String uid) {
		try (Statement tmp = conn.createStatement()) {
			MonitorFrame.toConsole("Проверяем наличие UID клиента '" + uid + "'...");
			try (ResultSet resSet = tmp.executeQuery("SELECT id FROM clients WHERE clients.uid='" + uid + "';")) {
				resSet.next();
				if (resSet.getRow() > 0) {
					MonitorFrame.toConsole("UID клиента успешно обнаружен в базе: row #" + resSet.getRow() + ".");
					return true;
				}
			} catch (SQLException e) {MonitorFrame.toConsole("UID клиента ошибка 1): " + e.getMessage());}
		} catch (SQLException e) {MonitorFrame.toConsole("UID клиента ошибка 2): " + e.getMessage());}
		
		MonitorFrame.toConsole("UID не обнаружен в базе!");
		return false;
	}
	
	// -------- Получить имя клиента по его UID --------
	public static String getClientNameByUID(String uid) {
		try (Statement tmp = conn.createStatement()) {
			MonitorFrame.toConsole("Проверяем наличие клиента c UID '" + uid + "'...");
			try (ResultSet resSet = tmp.executeQuery("SELECT name FROM clients WHERE clients.uid='" + uid + "';")) {
				resSet.next();
				if (resSet.getRow() > 0) {
					MonitorFrame.toConsole("Имя клиента успешно обнаружено в базе: row #" + resSet.getRow() + ".");
					return resSet.getString("name");
				}
			} catch (SQLException e) {MonitorFrame.toConsole("Имя клиента ошибка 1): " + e.getMessage());}
		} catch (SQLException e) {MonitorFrame.toConsole("Имя клиента ошибка 2): " + e.getMessage());}
		
		return null;
	}
	
	// -------- Проверка пароля юзера -------
	public static boolean checkPassword(String name, String uid, String password) {
		MonitorFrame.toConsole("Проверка пароля клиента '" + name + "': '" + password + "'...");
		
		String pass = null;
		try (Statement tmp = conn.createStatement()) {
			try (ResultSet resSet = tmp.executeQuery("SELECT password FROM clients WHERE clients.uid='" + uid + "';")) {
				resSet.next();
				pass = resSet.getString(1);
				if (pass.equals(password)) {
					MonitorFrame.toConsole("Пароль клиента верен. Всё в порядке, продолжаем...");
					return true;
				}
			} catch (SQLException e) {MonitorFrame.toConsole("Пароль клиента ошибка 1): " + e.getMessage());}
		} catch (SQLException e) {MonitorFrame.toConsole("Пароль клиента ошибка 2): " + e.getMessage());}
		
		MonitorFrame.toConsole("Внимание! Пароль клиента не верен! Верный пароль: '" + pass + "', принят пароль: '" + password + "'!");
		return false;
	}
	
	// -------- Обновление данных юзера -------
	public void changeClient(String currentName, String currentPass, String newName, String newPass) {
		MonitorFrame.toConsole("Обновление данных клиента '" + currentName + "'...");
		
	}
	
	// -------- Удаление клиента --------
	public void removeClient(String removableClientName) {
		try (Statement statmt = conn.createStatement()) {
			int correct = statmt.executeQuery("SELECT EXISTS (SELECT * FROM clients WHERE name='" + removableClientName + "' LIMIT 1);").getInt(1);
			
			if (correct != 0) {
				statmt.execute("DELETE FROM clients WHERE clients.name='" + removableClientName + "';");
				MonitorFrame.toConsole("Удаление клиента '" + removableClientName + "' завершено.");
			} else {MonitorFrame.toConsole("Не существует '" + removableClientName + "' в базе!");}
			
			statmt.close();
		} catch (SQLException e) {e.printStackTrace();}		
	}
	
	
	// -------- Закрытие базы --------
	public static void closeDB() {
		try {
			conn.close();
			Out.Print(DataBase.class, 0, "Соединение c DB успешно завершено.");
			conn = null;
		} catch (SQLException e) {e.printStackTrace();}		
	}

	
	
	
//	public static getTypeList() {
//		try (Statement tmp = conn.createStatement()) {
//			try (ResultSet resSet = tmp.executeQuery("SELECT * FROM 'type';")) {
//				while(resSet.next())	{result.add(resSet.getString("typename"));}
//			} catch (Exception e) {e.printStackTrace();}
//		} catch (SQLException e) {e.printStackTrace();}
//	}
	
//	public static String[][] getElementsData() {
//		try (Statement tmp = conn.createStatement()) {
//			ResultSet resSet = tmp.executeQuery("SELECT 'id' FROM 'aids';");
//			
//			elementsCount = 0;
//			while(resSet.next())	{elementsCount++;}
//			allData = new String[elementsCount][14];
//			
//			int c = 0;
//			resSet = tmp.executeQuery("SELECT * FROM 'aids';");
//			while(resSet.next())	{
//				int id = resSet.getInt("id");
//				int type = resSet.getInt("type");
//				
//				if (type < 0) {
//					System.err.println("DataBase: getElementsData(): ERR: type is " + type);
//					break;
//				}
//				
//				allData[c][0] = String.valueOf(id);
//				Statement tmp2 = conn.createStatement();
//				allData[c][1] = tmp2.executeQuery("SELECT * FROM type WHERE id=" + type + ";").getString("typename");
//				tmp2.close();
//				
//				allData[c][2] = resSet.getString("name");
//				allData[c][3] = resSet.getString("description");
//				allData[c][4] = resSet.getString("picpath");
//				allData[c][5] = String.valueOf(resSet.getDouble("modificКРС"));
//
//				c++;
//			}
//		} catch (SQLException e) {e.printStackTrace();}
//	}
}