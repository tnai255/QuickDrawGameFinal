package nz.ac.auckland.se206;

import java.util.HashMap;
import javafx.scene.Parent;
import javafx.scene.Scene;
import nz.ac.auckland.se206.util.EventEmitter;
import nz.ac.auckland.se206.util.EventListener;

public class ViewManager<V extends Enum<V>> {
  public interface ViewChangeSubscription<V> {
    public void run(V view);
  }

  private EventEmitter<V> viewChangeEmitter = new EventEmitter<V>();
  private HashMap<V, Parent> viewMap = new HashMap<V, Parent>();
  private Scene scene;
  private V currentlyLoadedView;

  /**
   * Constructs a new view manager. The view manager is intentionally tied to one scene
   *
   * @param scene this is a scene that the view manager will handle (control the root of)
   */
  public ViewManager(Scene scene) {
    this.scene = scene;
  }

  /**
   * This adds a new view that the manager will keep track of.
   *
   * @param view the user defined id of the view
   * @param root the parent node that will be reused
   */
  public void addView(V view, Parent root) {
    viewMap.put(view, root);
  }

  /**
   * This removes a view from the view manager. All data of the related node will be lost
   *
   * @param view the id of the view to remove
   */
  public void removeView(V view) {
    viewMap.remove(view);
  }

  /**
   * Sets the scene to the root associated with this view If the given view id has no associated
   * parent, it will leave the current view
   *
   * @param view the id of the view to load onto the scene
   */
  public void loadView(V view) {
    Parent parent = viewMap.get(view);
    currentlyLoadedView = view;

    if (parent != null) {
      scene.setRoot(parent);

      // Runs all registered subscription functions
      viewChangeEmitter.emit(view);
    }
  }

  /**
   * This method gives the view enum category which represents the currently loaded view.
   *
   * @return the view.
   */
  public V getCurrentlyLoadedView() {
    return currentlyLoadedView;
  }

  /**
   * This method will get the current scene
   *
   * @return current scene used
   */
  public Scene getScene() {
    return scene;
  }

  /**
   * Use this method when we want to add a listener to viewChangeEmitter to keep track if we have
   * switched views
   *
   * @param listener the EventListener to be notified when an event is emitted
   * @return the subscription ID for unsubscribing
   */
  public int subscribeToViewChange(EventListener<V> listener) {
    return viewChangeEmitter.subscribe(listener);
  }

  /**
   * This method allows us to remove a listener/listeners from our viewChangeEmitter using their
   * subscription ID.
   *
   * @param subId the subscription ID for unsubscribing
   */
  public void unsubscribeFromViewChange(int subId) {
    viewChangeEmitter.unsubscribe(subId);
  }
}
