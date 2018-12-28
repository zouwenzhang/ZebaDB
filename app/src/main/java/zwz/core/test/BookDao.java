package zwz.core.test;

import zwz.core.db.DbRepository;

public class BookDao extends DbRepository<BookRecord> {
    private static BookDao instance;

    public static BookDao get(){
        if(instance==null){
            instance=new BookDao();
        }
        return instance;
    }


}
