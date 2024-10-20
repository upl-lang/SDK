	/*
	 * Copyright (c) 2023 UPL Foundation
	 */
	
	package upl.db;
	
	import java.io.File;
	import java.net.MalformedURLException;
	import java.net.URL;
	import java.net.URLClassLoader;
	import java.sql.Connection;
	import java.sql.DriverManager;
	import java.sql.DriverPropertyInfo;
	import java.sql.SQLException;
	import java.sql.SQLFeatureNotSupportedException;
	import java.util.Properties;
	import java.util.logging.Logger;
	
	public class Driver implements java.sql.Driver {
		
		protected java.sql.Driver driver;
		protected URLClassLoader classLoader;
		
		public Driver (String file) throws MalformedURLException {
			this (new File (file));
		}
		
		public Driver (File file) throws MalformedURLException {
			this (file.toURI ().toURL ());
		}
		
		public Driver (URL... urls) {
			classLoader = new URLClassLoader (urls, getClass ().getClassLoader ());
		}
		
		public void load (String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
			
			driver = (java.sql.Driver) Class.forName (name, true, classLoader).newInstance ();
			
			DriverManager.registerDriver (this);
			
		}
		
		@Override
		public Connection connect (String url, Properties info) throws SQLException {
			return driver.connect (url, info);
		}
		
		@Override
		public boolean acceptsURL (String url) throws SQLException {
			return driver.acceptsURL (url);
		}
		
		@Override
		public DriverPropertyInfo[] getPropertyInfo (String url, Properties info) throws SQLException {
			return driver.getPropertyInfo (url, info);
		}
		
		@Override
		public int getMajorVersion () {
			return driver.getMajorVersion ();
		}
		
		@Override
		public int getMinorVersion () {
			return driver.getMinorVersion ();
		}
		
		@Override
		public boolean jdbcCompliant () {
			return driver.jdbcCompliant ();
		}
		
		@Override
		public Logger getParentLogger () throws SQLFeatureNotSupportedException {
			return driver.getParentLogger ();
		}
		
	}