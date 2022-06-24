package threadBase.JUC;
import org.apache.ibatis.io.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import threadBase.JUC.model.Student;


import java.io.InputStream;

/**
 * @author: Zekun Fu
 * @date: 2022/6/24 11:22
 * @Description:
 */
public class TestGenericDao {
    public static void main(String[] args) throws Exception{
        InputStream in = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in);
        SqlSession session = factory.openSession();
        Student student = session.selectOne("testStudent.getById", 1);
        System.out.println(student);

        session.close();
    }
}
