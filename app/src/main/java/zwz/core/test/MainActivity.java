package zwz.core.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import core.zwz.zwzcoredb.R;

import com.zeba.db.ZDbWhere;
import com.zeba.db.sl.ZebaSQLite;

public class MainActivity extends AppCompatActivity {

    private TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLog=findViewById(R.id.tv_log);
//        BookDao.get().init(getApplication(),"mb",1);
//        addOne();
        ZebaSQLite.init(this,"dd");
        new TestDb2().test(tvLog);
    }

    private void addOne(){
        try{
            BookRecord record=new BookRecord();
            record.setName("aaa");
            record.setBookType(1);
            record.setBookTime("123");
            record.setBookData("teset");
            record.setBookDataByte("tesetbyte".getBytes("utf-8"));
            BookDao.get().addOne(record,(r)->{
                tvLog.setText(tvLog.getText()+"addOne OK\n");
                addList();
            },(msg)->{
                tvLog.setText(tvLog.getText()+"addOne Error:"+msg+"\n");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addList(){
        try {
            List<BookRecord> list=new ArrayList<>();
            for(int i=0;i<2;i++){
                BookRecord record=new BookRecord();
                record.setName("aaa"+i);
                record.setBookType(i+1);
                record.setBookTime("123"+i);
                record.setBookData("teset"+i);
                record.setBookDataByte((i+"tesetbyte").getBytes("utf-8"));
                list.add(record);
            }
            BookDao.get().addList(list,(r)->{
                tvLog.setText(tvLog.getText()+"addList OK\n");
                findOne();
            },(msg)->{
                tvLog.setText(tvLog.getText()+"addList Error:"+msg+"\n");
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void findOne(){
        BookDao.get().findOne(new ZDbWhere("time").equ("123"),(data)->{
            if(data!=null){
                tvLog.setText(tvLog.getText()+"findOne OK\n");
                findAll();
            }
        },(msg)->{
            tvLog.setText(tvLog.getText()+"findOne Error:"+msg+"\n");
        });
    }

    private void findAll(){
        BookDao.get().findAll((data)->{
            if(data!=null){
                List<BookRecord> list=data.getData();
                for(int i=0;i<list.size();i++){
                    tvLog.setText(tvLog.getText()+"a1:"+list.get(i).getBookData()+"a2:"+list.get(i).getBookDataByteString()+"\n");
                }
                tvLog.setText(tvLog.getText()+"findAll OK count:"+data.getCount()+","+data.getData().size()+"\n");
            }
        },(msg)->{
            tvLog.setText(tvLog.getText()+"findAll Error:"+msg+"\n");
        });
    }

}
