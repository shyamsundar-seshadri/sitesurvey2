package hello;

import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;


public class DbUtility {
	
	   public static String dbname = "local";
	  
		public static MongoClient getClient() {
			MongoClient client = null;
			String uri = "";// "mongodb://localhost:27017";
			String svcs = System.getenv("VCAP_SERVICES");
//			if (svcs==null) {
//				try {
//					JSONObject json = new JSONObject(svcs);
//					 uri = json.getJSONArray("mongolab").getJSONObject(0)
//						      .getJSONObject("credentials").getString("uri");
//					dbname = "sitesurvey";
//				} catch (Exception e) {
//					// TODO: handle exception
//				}
//			
//			} else {
				uri = "mongodb://admin:password@ds017514.mlab.com:17514/sitesurvey";
				dbname = "sitesurvey";
			//}
			System.out.println("DBURI:"+uri);
			client = new MongoClient(new MongoClientURI(uri));
			System.out.println("Connected to DB");
			return client;
		}
		
		public static Document getDocFromMap(Map<String,Object> map) {
			Document      doc = new Document();
			for (String key:map.keySet()) {
				Object o = map.get(key);
				if (o instanceof java.util.Map)  return getDocFromMap(
						 (Map<String,Object>)o);
				else
					doc.put(key, o);
			}
			return doc;
		}
		
		public static long updateDoc(MongoDatabase db,
				                        String name,
				                        Document src,
				                        Map<String,Object> map) {
			long       rc = 0L;
			Document     doc = getDocFromMap(map);
			UpdateResult ur  = db.getCollection(name).updateOne(src,
					new Document("$set",doc));
			rc = ur.getMatchedCount();
			return rc;		
		}
		
		public static String insertDoc(MongoDatabase db,
				                        String name,
				                        Map<String,Object> map) {
			String       rc = null;
			Document     doc = getDocFromMap(map);
			db.getCollection(name).insertOne(doc);
			rc = doc.get("_id").toString();
			return rc;	
		}
		
		public static MongoCursor<Document> queryDoc(MongoDatabase db,
				                        String name,
				                        Map<String,Object> map) {
			MongoCursor<Document>  mc = null;
			Document     doc = getDocFromMap(map);
			mc = db.getCollection(name).find(doc).iterator();
			return mc;
		}
		
		
	}
