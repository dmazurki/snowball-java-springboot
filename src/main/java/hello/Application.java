package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.api.core.ApiFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.bigquery.storage.v1.*;
import com.google.protobuf.Descriptors;
import org.json.JSONArray;
import org.json.JSONObject;
 
import java.io.IOException;
import java.time.Instant;


import javax.management.loading.PrivateClassLoader;

@SpringBootApplication
@RestController
public class Application {

    static class WriteCommittedStream {

        final JsonStreamWriter jsonStreamWriter;
    
        public WriteCommittedStream(String projectId, String datasetName, String tableName) throws IOException, Descriptors.DescriptorValidationException, InterruptedException {
    
          try (BigQueryWriteClient client = BigQueryWriteClient.create()) {
    
            WriteStream stream = WriteStream.newBuilder().setType(WriteStream.Type.COMMITTED).build();
            TableName parentTable = TableName.of(projectId, datasetName, tableName);
            CreateWriteStreamRequest createWriteStreamRequest =
                    CreateWriteStreamRequest.newBuilder()
                            .setParent(parentTable.toString())
                            .setWriteStream(stream)
                            .build();
    
            WriteStream writeStream = client.createWriteStream(createWriteStreamRequest);
    
            jsonStreamWriter = JsonStreamWriter.newBuilder(writeStream.getName(), writeStream.getTableSchema()).build();
          }
        }
    
        public ApiFuture<AppendRowsResponse> send(Arena arena) {
          Instant now = Instant.now();
          JSONArray jsonArray = new JSONArray();
    
          arena.state.forEach((url, playerState) -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("x", playerState.x);
            jsonObject.put("y", playerState.y);
            jsonObject.put("direction", playerState.direction);
            jsonObject.put("wasHit", playerState.wasHit);
            jsonObject.put("score", playerState.score);
            jsonObject.put("player", url);
            jsonObject.put("timestamp", now.getEpochSecond() * 1000 * 1000);
            jsonArray.put(jsonObject);
          });
    
          return jsonStreamWriter.append(jsonArray);
        }
    
      }
    
      final String projectId = ServiceOptions.getDefaultProjectId();
      final String datasetName = "snowball";
      final String tableName = "events";
    
      final WriteCommittedStream writeCommittedStream;
    
      public Application() throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
        writeCommittedStream = new WriteCommittedStream(projectId, datasetName, tableName);
      }

      

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
    writeCommittedStream.send(arenaUpdate.arena);
    String myHref = arenaUpdate._links.self.href;
    PlayerState myState = arenaUpdate.arena.state.get(myHref);
    if (myState.x <= 0 && !"E".equals(myState.direction)) {
        return "R";
    }  
    if (myState.x >= (arenaUpdate.arena.dims.get(0) - 1) && !"W".equals(myState.direction)) {
        return "R";
    }
    if (myState.y <= 0 && !"S".equals(myState.direction)) {
        return "R";
    }
    if (myState.y >= (arenaUpdate.arena.dims.get(1) - 1) && !"N".equals(myState.direction)) {
        return "R";
    }

    if ( (myState.x <= 0) || (myState.x >= (arenaUpdate.arena.dims.get(0) - 1)) || (myState.y <= 0 ) || (myState.y >= (arenaUpdate.arena.dims.get(1) - 1))) {
        return "F";
    }
    for (PlayerState other : arenaUpdate.arena.state.values()) {
        Relation relation = new Relation(myState, other);
        if (relation.shootRisk) {
            Direction myDir = Direction.valueOf(myState.direction);
            if (myDir == relation.myExpectedDirection) {
                return "T";
            } 
            return "F";

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