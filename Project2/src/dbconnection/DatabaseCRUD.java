package dbconnection;

import org.bson.Document;

import com.mongodb.client.MongoCursor;

public class DatabaseCRUD extends DBConnectionDAO{
	//insert one document to the database
	public void insert(Document document) {
		collection.insertOne(document);
	}	
	
	//returns all documents in the collection
	public MongoCursor<Document> getAllDocs()
	{
		return collection.find().iterator();
	}
	
	//returns the number of documents in the collection
	public int getCollectionCount() {
		return (int)collection.count();
	}
	
	//returns the maximum group no
	public int getMaxGroupNo() { 
		Document docMaxGroupNo = collection.find().sort(new Document().append("groupNo", -1)).limit(1).iterator().next();
		int maxGroupNo = Integer.parseInt(docMaxGroupNo.get("groupNo").toString());
		return maxGroupNo;
	}
	
	//updates one document with update document field depending on id number 
	public void update(String id, Document newValue)
	{
		//creates filter document with id number
		Document filter = new Document().append("_id", id);
	
		collection.findOneAndUpdate(filter, newValue);
	}
	
	public void delete(String id)
	{
		//creates filter document with id number
		Document filter = new Document().append("_id", id);
		
		collection.deleteOne(filter);
	}
}
