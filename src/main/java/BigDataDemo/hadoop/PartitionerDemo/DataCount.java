package BigDataDemo.hadoop.PartitionerDemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by muyux on 2015/11/30.
 */
public class DataCount {
    private static class DCMapper extends Mapper<LongWritable, Text, Text, DataBean> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //accept
            String line = value.toString();
            //split
            String[] fields = line.split("\t");
            String tel = fields[1];
            long up = Long.parseLong(fields[8]);
            long down = Long.parseLong(fields[9]);
            DataBean bean = new DataBean(tel, up, down);
            //send
            context.write(new Text(tel), bean);
        }
    }

    private static class DCReducer extends Reducer<Text, DataBean, Text, DataBean> {

        @Override
        protected void reduce(Text key, Iterable<DataBean> values, Context context) throws IOException, InterruptedException {
            long up_sum = 0;
            long down_sum = 0;
            for (DataBean bean : values) {
                up_sum += bean.getUpPayLoad();
                down_sum += bean.getDownPayLoad();
            }
            DataBean bean = new DataBean(key.toString(), up_sum, down_sum);
            context.write(key, bean);
        }
    }

    private static class DCPartitioner extends Partitioner<Text, DataBean> {
        private static Map<String, Integer> provider = new HashMap<String, Integer>();

        static {
            provider.put("138", 1);
            provider.put("139", 1);
            provider.put("152", 2);
            provider.put("153", 2);
            provider.put("182", 3);
            provider.put("183", 3);
        }

        @Override
        public int getPartition(Text text, DataBean dataBean, int numPartitions) {
            //向数据库或配置信息 读写
            String tel_sub = text.toString().substring(0, 3);
            Integer count = provider.get(tel_sub);
            if (count == null) {
                count = 0;
            }
            return count; //返回的值决定这个值对应的数据给那个reducer处理，一个reducer对应于一个文件
        }
    }


    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf);

            job.setJarByClass(DataCount.class);

            job.setMapperClass(DCMapper.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(DataBean.class);
            FileInputFormat.setInputPaths(job, new Path(args[0]));   //数据文件地址

            job.setReducerClass(DCReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(DataBean.class);
            FileOutputFormat.setOutputPath(job, new Path(args[1]));  //保存结果的地址

            job.setNumReduceTasks(Integer.parseInt(args[2]));  //启动 reducer 的数量，一个reducer对应于一个文件

            job.setPartitionerClass(DCPartitioner.class);

            job.waitForCompletion(true);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

    }
}
