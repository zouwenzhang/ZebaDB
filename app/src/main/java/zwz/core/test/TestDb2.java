package zwz.core.test;

import android.widget.TextView;

public class TestDb2 {
    private BookDao2 bookDao;
    private TextView tvLog;
    private StringBuilder log=new StringBuilder();
    public void test(TextView textView){
        tvLog=textView;
        try {
            bookDao=new BookDao2();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addOne();
    }

    private void log(String text){
        log.append(text+"\n");
        tvLog.setText(log);
    }

    public void addOne(){
        BookRecordV2 record=new BookRecordV2();
        record.setName("aaa");
        record.setBookType(1);
        record.setBookTime("123");
        record.setBookData("teset");
        bookDao.add(record,r->{
            log("add success");
        },err->{
            log("add fail "+err.getMessage());
        });
    }

}
