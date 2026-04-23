import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static MongoCollection<Document> feedbackColl;
    private static MongoCollection<Document> featureColl;

    public static void main(String[] args) {
        // MongoDB Connection
        MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase db = mongoClient.getDatabase("feedback_system");
        feedbackColl = db.getCollection("feedback");
        featureColl = db.getCollection("feature_requests");

        System.out.println("✅ Connected to MongoDB successfully!");
        System.out.println("Welcome to Product Feedback Loop System 🚀\n");

        // Menu-driven system
        int choice;
        do {
            System.out.println("========== MENU ==========");
            System.out.println("1. Add Feedback");
            System.out.println("2. View Feedback");
            System.out.println("3. Update Feedback");
            System.out.println("4. Delete Feedback");
            System.out.println("5. Categorize Feedback Automatically");
            System.out.println("6. Add Feature Request");
            System.out.println("7. View Feature Requests");
            System.out.println("8. Prioritize Feature Requests");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");
            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1: addFeedback(); break;
                case 2: viewFeedback(); break;
                case 3: updateFeedback(); break;
                case 4: deleteFeedback(); break;
                case 5: categorizeFeedback(); break;
                case 6: addFeatureRequest(); break;
                case 7: viewFeatureRequests(); break;
                case 8: prioritizeFeatures(); break;
                case 9: System.out.println("👋 Exiting... Goodbye!"); break;
                default: System.out.println("❌ Invalid choice! Try again.");
            }
        } while (choice != 9);

        mongoClient.close();
    }

    // ---------------------- FEEDBACK CRUD ---------------------- //
    private static void addFeedback() {
        System.out.print("Enter user name: ");
        String user = sc.nextLine();
        System.out.print("Enter feedback text: ");
        String text = sc.nextLine();

        Document doc = new Document("user", user)
                .append("text", text)
                .append("category", "")
                .append("createdAt", new Date());
        feedbackColl.insertOne(doc);

        System.out.println("✅ Feedback added successfully!\n");
    }

    private static void viewFeedback() {
        System.out.println("\n--- All Feedback ---");
        for (Document d : feedbackColl.find()) {
            System.out.println(d.toJson());
        }
        System.out.println();
    }

    private static void updateFeedback() {
        System.out.print("Enter Feedback ID to update: ");
        String id = sc.nextLine();

        try {
            ObjectId oid = new ObjectId(id);
            System.out.print("Enter new feedback text: ");
            String newText = sc.nextLine();

            feedbackColl.updateOne(Filters.eq("_id", oid), Updates.set("text", newText));
            System.out.println("✅ Feedback updated!\n");
        } catch (Exception e) {
            System.out.println("❌ Invalid ID.\n");
        }
    }

    private static void deleteFeedback() {
        System.out.print("Enter Feedback ID to delete: ");
        String id = sc.nextLine();

        try {
            ObjectId oid = new ObjectId(id);
            feedbackColl.deleteOne(Filters.eq("_id", oid));
            System.out.println("🗑️ Feedback deleted!\n");
        } catch (Exception e) {
            System.out.println("❌ Invalid ID.\n");
        }
    }

    // ---------------------- AUTO CATEGORIZATION ---------------------- //
    private static void categorizeFeedback() {
        System.out.println("\nCategorizing feedback...");
        for (Document d : feedbackColl.find()) {
            String text = d.getString("text").toLowerCase();
            String category;

            if (text.contains("error") || text.contains("bug") || text.contains("issue"))
                category = "Bug";
            else if (text.contains("add") || text.contains("feature") || text.contains("improve"))
                category = "Enhancement";
            else
                category = "General";

            feedbackColl.updateOne(Filters.eq("_id", d.getObjectId("_id")),
                    Updates.set("category", category));
        }
        System.out.println("✅ Feedback categorized successfully!\n");
    }

    // ---------------------- FEATURE REQUEST CRUD ---------------------- //
    private static void addFeatureRequest() {
        System.out.print("Enter feature name: ");
        String name = sc.nextLine();
        System.out.print("Enter description: ");
        String desc = sc.nextLine();
        System.out.print("Enter demand level (1-10): ");
        int demand = Integer.parseInt(sc.nextLine());
        System.out.print("Enter feasibility (1-10): ");
        int feasibility = Integer.parseInt(sc.nextLine());
        System.out.print("Enter impact (1-10): ");
        int impact = Integer.parseInt(sc.nextLine());

        Document doc = new Document("name", name)
                .append("description", desc)
                .append("demand", demand)
                .append("feasibility", feasibility)
                .append("impact", impact)
                .append("votes", 0)
                .append("createdAt", new Date());
        featureColl.insertOne(doc);

        System.out.println("✅ Feature Request added!\n");
    }

    private static void viewFeatureRequests() {
        System.out.println("\n--- Feature Requests ---");
        for (Document d : featureColl.find()) {
            System.out.println(d.toJson());
        }
        System.out.println();
    }

    // ---------------------- PRIORITIZATION ---------------------- //
    private static void prioritizeFeatures() {
        System.out.println("\n--- Prioritized Feature Requests ---");

        List<Document> all = featureColl.find().into(new ArrayList<>());
        for (Document d : all) {
            int demand = d.getInteger("demand", 0);
            int feasibility = d.getInteger("feasibility", 0);
            int impact = d.getInteger("impact", 0);
            int votes = d.getInteger("votes", 0);

            double score = (demand * 0.4) + (impact * 0.3) + (feasibility * 0.2) + (votes * 0.1);
            featureColl.updateOne(Filters.eq("_id", d.getObjectId("_id")),
                    Updates.set("score", score));
        }

        List<Document> ranked = featureColl.find()
                .sort(new Document("score", -1))
                .into(new ArrayList<>());

        for (Document d : ranked) {
            System.out.println("Feature: " + d.getString("name") +
                    " | Score: " + d.getDouble("score"));
        }
        System.out.println();
    }
}
