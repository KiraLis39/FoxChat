package door;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import fox.adds.Out;
import gui.MonitorFrame;
import server.Server;


public class MainClass {
	private static Logger log;
	
	public static void main(String[] args) {
		Out.setLogsCoutAllow(3);
		
		setupLog4();
		log.trace("Log trace...");
		log.info("Log info...");
		log.debug("Log debug...");
		log.warn("Log warn...");
		log.error("Log error...");
		log.fatal("Log fatal...");
		
		new Server(); // Инициализация и подготовка к работе класса "Сервер".
		new MonitorFrame(); // Запуск фрейма "Монитор" и старт сервера.
	}

	private static void setupLog4() {
        // configures the root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.TRACE);
        
        rootLogger.addAppender(new ConsoleAppender() {
        	{
        		setLayout(new PatternLayout("%-7p %d{dd MMM yyyy HH:mm:ss} [%t] %c{2} (Line:%L)%nMessage:'%m'%n%n")); // {dd MMM yyyy HH:mm:ss,SSS} // {yyyy-MM-dd HH:mm:ss}
        		activateOptions();
        	}
        });
        rootLogger.addAppender(new FileAppender() {
        	{
        		setFile("./log4/log4j.log");
        		setLayout(new PatternLayout("%-7p %d{dd MMM yyyy HH:mm:ss} [%t] %c{2} (Line:%L)%nMessage:'%m'%n%n")); // {dd MMM yyyy HH:mm:ss,SSS} // {yyyy-MM-dd HH:mm:ss}
//        		setLayout(new HTMLLayout()); // "%-6p %d{yyyy-MM-dd HH:mm:ss} [%t] %c{2} (Line:%L)%nMessage:'%m'%n"
                activateOptions();
        	}
        });
 
        // creates a custom logger and log messages
        log = LogManager.getLogger(MainClass.class);
	}
}