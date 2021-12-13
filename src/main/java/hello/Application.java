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
    public int distanceHorizontal;
    public int directionHorizontal;
    public int distanceVertical;
    public int directionVertical;
    public int distance;
    public boolean shootRisk;
    public boolean shootOpportunity;
    public Direction myExpectedDirection;
    public Direction opponentExpectedDirection;




    Relation(PlayerState me, PlayerState other) {
         distanceHorizontal = Math.abs(other.x - me.x);
         directionHorizontal = (other.x - me.x > 0) ? 1 : -1;
         distanceVertical = Math.abs(other.y - me.y);
         directionVertical = (other.y - me.y > 0) ? 1 : -1;
         distance = distanceHorizontal + distanceVertical;
         myExpectedDirection = getExpectedDirection();
         opponentExpectedDirection = myExpectedDirection.getOpposite();


         Direction opponentDirection  = Direction.valueOf(other.direction);
         if (opponentDirection == opponentExpectedDirection 
         && ((distanceVertical < 4 && distanceHorizontal == 0) || (distanceHorizontal < 4 && distanceVertical == 0))) {
            shootRisk = true;
         }
    }

    private Direction getExpectedDirection() {
        if (distanceHorizontal < distanceVertical) {
            return (directionHorizontal > 0) ? Direction.E : Direction.W;
        } else {
            return (directionVertical > 0) ? Direction.S : Direction.N;
        }
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
    return "Let the battle begin K8S!";
  }

  @PostMapping("/**")
  public String index(@RequestBody ArenaUpdate arenaUpdate) {
    String myHref = arenaUpdate._links.self.href;
    PlayerState myState = arenaUpdate.arena.state.get(myHref);

    for (PlayerState other : arenaUpdate.arena.state.values()) {
        Relation relation = new Relation(myState, other);
        if (relation.shootRisk) {
            if ((Direction.valueOf(myState.direction) == Direction.valueOf(other.direction).getOpposite())) {
                return "R";
            } 
            return "T";

        }

    }
    int rand = new Random().nextInt() % 100;
    if (rand < 80) {
        return "F";
    } else if (rand < 90) {
        return "L";
    } else {
        return "R";
    }
  }

  

 

}
enum Direction {
    N, W, S,E;

    public Direction getOpposite() {
        switch(this) {
            case N: return S;
            case E: return W;
            case S: return N;
            default: return E;
        }
    }

    public Direction turn() {
        switch(this) {
            case N: return E;
            case E: return S;
            case S: return W;
            default: return N;
        }
    }
}