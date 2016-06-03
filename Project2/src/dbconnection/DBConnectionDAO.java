package dbconnection;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DBConnectionDAO {
	protected MongoDatabase db;
	protected MongoClient mongoClient;
	protected MongoCollection<Document> collection;

	public void openDB() 
	{
		// connect to mongoDB, ip and port number
		mongoClient = new MongoClient();

		// get database from MongoDB,
		// if database doesn't exists, mongoDB will create it automatically
		db = mongoClient.getDatabase("mail");
	}
	
	public boolean connectDBCollection(String collectionName) {
		if(collectionName == null || collectionName.isEmpty()) {
			return false;
		}
		
		collection = db.getCollection(collectionName);
		return true;
	}
	
	public void closeDB() {
		mongoClient.close();
	}
}
