package zwz.core.test;

import com.zeba.db.DbSimpleRepository;

public class BookDao extends DbSimpleRepository<BookRecord> {
    private static BookDao instance;

    public static BookDao get(){
        if(instance==null){
            instance=new BookDao();
        }
        return instance;
    }


}
