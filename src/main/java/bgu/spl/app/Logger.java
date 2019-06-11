package bgu.spl.app;

public class Logger {
	private LogLevel level;
	
	private static class SingletonHolder {
        private static Logger instance = new Logger();
    }
	
    private Logger() {
    	this.level = LogLevel.INFO;
    }
    
    public static Logger getInstance() {
        return SingletonHolder.instance;
    }
    
    public void error(String logMessage) {
    	logMessage(logMessage, LogLevel.ERROR);
    }
    
    public void warn(String logMessage) {
    	logMessage(logMessage, LogLevel.WARN);
    }
    
    public void log(String logMessage) {
    	logMessage(logMessage, LogLevel.INFO);
    }
    
    public void debug(String logMessage) {
    	logMessage(logMessage, LogLevel.DEBUG);
    }
	
	private void logMessage(String logMessage, LogLevel logLevel) {
		if (logLevel.getLevel() <= level.getLevel()) {
			System.out.println(logMessage);
		}
    }
	
	public void setLogLevel(LogLevel logLevel) {
		this.level = logLevel;
	}
}
