package nz.ac.auckland.se206.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will be used to handle any functionalities we would like to do with User class
 * objects/instances An example would be saving a newly created user profile into our json file
 */
public class UserManager {

  // instance fields
  private List<User> users = new ArrayList<User>();
  private int currentUserIndex = 0;
  private File userProfilesFile;

  /**
   * Call this method when it is required to create and save (serialise) a new user profile to json
   * file
   *
   * @param username name of user profile
   * @param colour chosen colour
   * @return boolean to indicate if creation was successful or not
   * @throws IOException
   * @throws URISyntaxException
   */
  public boolean createUserProfile(String username, String colour)
      throws IOException, URISyntaxException {

    // check if username already exists, return false
    if (userWithNameAlreadyExists(username)) {

      return false;

    } else { // username is unique and new user can successfully be created, return true

      // creation of new user instance, adding to our user list
      User user = new User(username, colour);
      users.add(user);

      serialise();

      // lets assume we want to use the newly created profile
      setCurrentProfile(user.getID());

      return true;
    }
  }

  /**
   * Use this method to update the username of the current user profile in use
   *
   * @param newName new username to update to
   * @return boolean to indicate if update was successful or not
   * @throws IOException
   * @throws URISyntaxException
   */
  public boolean updateUserName(String newName) throws IOException, URISyntaxException {

    // checking if username already exists, return false if it does
    if (userWithNameAlreadyExists(newName)) {

      return false;

    } else { // username is unique, return true

      this.users.get(currentUserIndex).changeUserName(newName);
      return true;
    }
  }

  /**
   * Use this method when you want to save the stats of an existing user profile into json
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  public void updateCurrentProfile() throws IOException, URISyntaxException {

    // store current user in use
    User currUser = this.users.get(currentUserIndex);

    // get and update list of all user profiles
    users = getExistingProfiles();

    // then replace User at given currentUserIndex in our list with updated values
    users.set(currentUserIndex, currUser);

    // serialise list to save changes
    serialise();
  }

  /**
   * Use this method to set the reference of currentUser to wanted user profile / object Can also be
   * used to switch between user profiles
   *
   * @param userID unique ID of the User profile we want to set to / use
   * @throws IOException
   * @throws URISyntaxException
   */
  public void setCurrentProfile(int userID) throws IOException, URISyntaxException {

    users = getExistingProfiles();

    int length = users.size();

    // iterate through and find user object with same ID and update our currentUserIndex instance
    // field
    for (int i = 0; i < length; i++) {

      if (users.get(i).getID() == userID) {
        currentUserIndex = i;
        break;
      }
    }
  }

  /**
   * Call this method when you want a list of all User objects that have been created and stored on
   * json file Can only call if the json file already exists
   *
   * @return arraylist of User objects
   * @throws IOException
   * @throws URISyntaxException
   */
  public List<User> getExistingProfiles() throws IOException, URISyntaxException {

    deserialise();
    return users;
  }

  /**
   * Getter method to get the current User object profile in use
   *
   * @return object of class User that is the current profile being used
   * @throws IOException
   * @throws URISyntaxException
   */
  public User getCurrentProfile() throws IOException, URISyntaxException {
    return getExistingProfiles().get(currentUserIndex);
  }

  /**
   * Use this method to get an object of class UserStats containing simple getter methods for user
   * to get wanted user stats Only use when a user profile has been set
   *
   * <p>Can simply use UserStats class methods to get and update desired stats
   *
   * @return object of class UserStats containing stats of the current user profile
   */
  public UserStats getUserStats() {

    return users.get(currentUserIndex).getUserStats();
  }

  /**
   * This method will handle serialising / saving to json file
   *
   * @throws IOException
   */
  private void serialise() throws IOException, URISyntaxException {

    Writer writer = new FileWriter(userProfilesFile);
    Gson gson = new GsonBuilder().create();
    gson.toJson(users, writer);

    // flushing and closing the writer (cleaning)
    writer.flush();
    writer.close();
  }

  /**
   * This method will handle deserialsing / loading from json file
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  private void deserialise() throws IOException, URISyntaxException {

    // deserialising file containing existing profiles
    // de-serialisation
    // create file reader
    userProfilesFile = new File(UserManager.class.getResource("/").getFile() + "UserProfiles.json");

    userProfilesFile.createNewFile();

    // doesn't deserialise if user profiles is empty
    if (!users.isEmpty()) {
      // firstly checking if the file exists
      try (Reader reader = new FileReader(userProfilesFile)) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<User>>() {}.getType();

        System.out.println("deserialise");

        // converting json string to arraylist of user objects
        users = gson.fromJson(reader, listType);

        System.out.println("deserialise" + users.get(0).getName());

        // close reader
        reader.close();
      } catch (FileNotFoundException e) {
        System.out.println("Error");
      }
    }
  }

  /**
   * Helper method to check for duplicate usernames
   *
   * @return boolean - True if a duplicate exists
   * @throws IOException
   * @throws URISyntaxException
   */
  private boolean userWithNameAlreadyExists(String userName)
      throws IOException, URISyntaxException {

    // update users
    users = getExistingProfiles();

    // iterate through all users and if username already exists, return true
    for (User user : users) {

      if (user.getName().equals(userName)) {
        return true;
      }
    }

    return false;
  }
}
