package nz.ac.auckland.se206.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * This class will be used to handle any functionalities we would like to do with User class objects/instances
 * An example would be saving a newly created user profile into our json file
 */
public class UserManager {
  
  // instance fields
  List<User> users = new ArrayList<User>();

  /**
   * Call this method when it is required to create and save (serialise) a new user profile to json file
   * @param username name of user profile
   * @param colour chosen user colour
   * @throws IOException
   */
  public void createUserProfile(String username, String colour) throws IOException{

    // creation of new user instance, adding to our user list
    User user = new User(username, colour);
    users.add(user);

    // serialising - saving our list of users to a json file using gson library
    try(Writer writer = new FileWriter("UserProfiles.json")) {
      Gson gson = new GsonBuilder().create();
      gson.toJson(users, writer);
    }

  }

  /**
   * Call this method when you want a list of all User objects that have been created and stored on json file
   * Can only call if the json file already exists
   * @return arraylist of User objects
   * @throws IOException
   */
  public List<User> getExistingProfiles() throws IOException{

    // deserialising file containing existing profiles
    // de-serialisation
    // create file reader
    Reader reader = Files.newBufferedReader(Paths.get("UserProfiles.json"));

    Gson gson = new Gson();
    Type listType = new TypeToken<ArrayList<User>>(){}.getType();
        
    // converting json string to arraylist of user objects
    List<User> listOfUsers = gson.fromJson(reader, listType);

    // close reader
    reader.close();

    return listOfUsers;
  }

}
