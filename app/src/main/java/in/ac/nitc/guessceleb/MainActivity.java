package in.ac.nitc.guessceleb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    int chosenCeleb;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    ArrayList<String> celebURL = new ArrayList<>();
    ArrayList<String> celebName = new ArrayList<>();
    int[] answers = new int[4];
    int rightAnswer;

    public void buttonPressed(View view)
    {
        int tag = Integer.parseInt(String.valueOf(view.getTag()));
        if(tag == rightAnswer)
        {
            Toast.makeText(getApplicationContext(),"Right!!",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Wrong!! Its " + celebName.get(chosenCeleb),Toast.LENGTH_SHORT).show();
        }
        getNewQuestion();
    }


    public void getNewQuestion()
    {
        Random rand = new Random();

        chosenCeleb = rand.nextInt(celebName.size());

        rightAnswer = rand.nextInt(4);

        for(int i=0;i<4;i++)
        {
            if(i == rightAnswer)
            {
                answers[i] = chosenCeleb;
            }
            else
            {
                int wrongAnswer = rand.nextInt(celebName.size());

                while(wrongAnswer == chosenCeleb)
                {
                    wrongAnswer = rand.nextInt(celebName.size());
                }
                answers[i] = wrongAnswer;
            }
        }

        ImageDownloader imageTask = new ImageDownloader();

        try {
            imageView.setImageBitmap(imageTask.execute(celebURL.get(chosenCeleb)).get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        button1.setText(celebName.get(answers[0]));
        button2.setText(celebName.get(answers[1]));
        button3.setText(celebName.get(answers[2]));
        button4.setText(celebName.get(answers[3]));

    }
    public class ImageDownloader extends  AsyncTask<String, Void, Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... strings) {

            URL url;
            HttpURLConnection connection;
            Bitmap bitmap;

            try
            {
                url = new URL(strings[0]);

                connection = (HttpURLConnection) url.openConnection();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            HttpURLConnection connection;
            String result = "";

            try
            {
                url  =  new URL(urls[0]);

                connection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = connection.getInputStream();

                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while (data != -1)
                {
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }

                return result;
            }
            catch(Exception e)
            {
                e.fillInStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);



        DownloadTask task = new DownloadTask();

        String html = "";

        try {
            html =task.execute("http://www.posh24.se/kandisar").get();

            String[] htmlSplit = html.split("div id=\"webx_most_popular\"");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(htmlSplit[0]);

            while(m.find())
            {
                celebURL.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"/>");
            m = p.matcher(htmlSplit[0]);

            while(m.find())
            {
                celebName.add(m.group(1));
            }


        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        getNewQuestion();

    }
}
