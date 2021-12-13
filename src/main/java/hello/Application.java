package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.management.loading.PrivateClassLoader;

@SpringBootApplication
@RestController
public class Application {

  static class Self {
    public String href;
  }

  static class Links {
    public Self self;
  }

  static class PlayerState {
    public Integer x;
    public Integer y;
    public String direction;
    public Boolean wasHit;
    public Integer score;
  }

  static class Arena {
    public List<Integer> dims;
    public Map<String, PlayerState> state;
  }

  static class ArenaUpdate {
    public Links _links;
    public Arena arena;
  }

  static class Relation {
    private int distanceHorizontal;
    private int directionHorizontal;
    private int distanceVertical;
    private int directionVertical;
    private int distance;
    boolean shootRisk;
    boolean shootOpportunity;
    private Direction myExpectedDirection;
    private Direction opponentExpectedDirection;


    Relation(PlayerState me, PlayerState other, List<Integer> dims) {
         distanceHorizontal = Math.abs(other.x - me.x);
         directionHorizontal = (other.x - me.x > 0) ? 1 : -1;
         distanceVertical = Math.abs(other.y - me.y);
         directionVertical = (other.y - me.y > 0) ? 1 : -1;
         distance = distanceHorizontal + distanceVertical;
    }

    private Direction getExpectedDirection() {
        return null;
    }

  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.initDirectFieldAccess();
  }

  @GetMapping("/")
  public String index() {
    return "Let the battle begin!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    String myHref = arenaUpdate._links.self.href;
    PlayerState myState = arenaUpdate.arena.state.get(myHref);

    System.out.println(arenaUpdate);
    String[] commands = new String[]{"F", "R", "L", "T"};
    int i = new Random().nextInt(4);
    return "T";
  }

  

 

}
enum Direction {
    N, W, S,E
}