import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class MeanNumClick {

    private String type = "QueryUrl";
    private HashSet<String> trainSet, testSet;
    private HashMap<String, Integer> urlId, queryId;

    MeanNumClick(HashSet<String> trainSet,
                  HashSet<String> testSet,
                  HashMap<String, Integer> urlId,
                  HashMap<String, Integer> queryId,
                  String type) {
        if (type.equals("Url")) {
            this.type = type;
        }
        this.urlId = urlId;
        this.queryId = queryId;
        this.trainSet = trainSet;
        this.testSet = testSet;
    }

    public void map(Mapper.Context context, SERP serp) throws IOException, InterruptedException {
        int i = 0;
        for(String clickUrl : serp.clicked) {
            i++;
            if (trainSet.contains(clickUrl) || testSet.contains(clickUrl)) {
                if (this.type.equals("QueryUrl")) {
                    if(queryId.containsKey(serp.query) && urlId.containsKey(clickUrl)) {
                        context.write(new Text(Config.MEAN_NUM_CLICK + Config.DELIMER + queryId.get(serp.query) + Config.DELIMER + urlId.get(clickUrl)),
                                new Text(String.valueOf(i)));
                    }
                } else {
                    if(urlId.containsKey(clickUrl)) {
                        context.write(new Text(Config.MEAN_NUM_CLICK + Config.DELIMER + urlId.get(clickUrl)),
                                new Text(String.valueOf(i)));
                    }
                }
            }
        }
    }

    public void reduce(Text key,
                       Iterable<Text> values,
                       Reducer.Context context) throws IOException, InterruptedException {

        double mean = 0, length = 0;
        for (Text value : values) {
            length++;
            mean = mean + Double.valueOf(value.toString());
        }
        mean = mean / length;

        if (this.type.equals("QueryUrl")) {
            int query_id = Integer.valueOf(key.toString().split(Config.DELIMER)[1]);
            int url_id = Integer.valueOf(key.toString().split(Config.DELIMER)[2]);
            context.write(new Text("QueryUrl\t"
                            + key.toString().split(Config.DELIMER)[0] + "\t"
                            + String.valueOf(query_id)) + "\t"
                            + String.valueOf(url_id),
                    new Text(String.valueOf(mean)));

        } else {
            int url_id = Integer.valueOf(key.toString().split(Config.DELIMER)[1]);
            context.write(new Text("Url\t"
                            + key.toString().split(Config.DELIMER)[0] + "\t"
                            + String.valueOf(url_id)),
                    new Text(String.valueOf(mean)));
        }
    }
}
