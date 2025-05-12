package model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named(value = "schoolBean")
@SessionScoped
//@ManagedBean
public class SchoolBean implements Serializable {
    private int n=1;
    private Integer Id;
    private String name;
    private String editSchoolId;    
    
    public static Statement stmtObj;
    public static Connection connObj;
    public static ResultSet resultSetObj;
    public static PreparedStatement pstmt;    
    private final static Logger LOGGER = Logger.getLogger(SchoolBean.class.getName());
    
    
    public SchoolBean() {
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEditSchoolId() {
        return editSchoolId;
    }

    public void setEditSchoolId(String editSchoolId) {
        this.editSchoolId = editSchoolId;
    }
    
    public static String time(){
        String pattern = "MM/dd/yyyy HH:mm:ss";

        // Create an instance of SimpleDateFormat used for formatting 
        // the string representation of date according to the chosen pattern
        DateFormat df = new SimpleDateFormat(pattern);

        // Get the today date using Calendar object.
        Date today = Calendar.getInstance().getTime();        
        // Using DateFormat format method we can create a string 
        // representation of a date with the defined format.
        String todayAsString = df.format(today);

        // Print the result!
        return todayAsString;        
    }
    public static  void log(String s){
       LOGGER.log(Level.INFO,time()+" "+s); 
    }
    public static Connection getConnection(){  
        try{  
        	System.out.println("INI**********************");
            Class.forName("oracle.jdbc.driver.OracleDriver");     
            String db_url ="jdbc:oracle:thin:@localhost:1521:xe",
                    db_userName = "jose",
                    db_password = "jose";
            connObj = DriverManager.getConnection(db_url,db_userName,db_password);  
        } catch(Exception sqlException) {  
            sqlException.printStackTrace();
            log("ERR al conectar");
            System.out.println("ERROR LA CONECTAR***********");
        }  
        return connObj;
    }   
    
    public List<SchoolBean> getList() {
    	List<SchoolBean> schoolList =  new ArrayList<SchoolBean>();  
        try {
        	 log("INI Total Records Fetched: " + schoolList.size());
        	 System.out.println("INI........................");

            stmtObj = getConnection().createStatement();    
            resultSetObj = stmtObj.executeQuery("SELECT id,name FROM School");
            
            while(resultSetObj.next()) {  
            	SchoolBean stuObj = new SchoolBean(); 
                stuObj.setId(resultSetObj.getInt("id"));  
                stuObj.setName(resultSetObj.getString("name"));  
                schoolList.add(stuObj);  
            }   
            log("Total Records Fetched: " + schoolList.size());
            System.out.println("Total Records Fetched: " + schoolList.size()); 
            connObj.close();
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
            System.out.println("ERROR ******** "+sqlException.getMessage());
        } 
        return schoolList;
    }        
  
	// Method To Add New School To The Database
	public String addNewSchool(SchoolBean schoolBean) {
                log(" addNewSchool 1********************: "+schoolBean.getName());
		return createNewSchool(schoolBean.getName());		
	}    
    public String createNewSchool(String nom){
        Integer Id=0;
        try {
            Id = getMaxSchoolId();           
 
           String sql = "insert into School (id,name) VALUES (?,?)";
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = connObj.prepareStatement(sql);
            preparedStmt.setInt    (1,Id);
            preparedStmt.setString (2,nom);

            // execute the preparedstatement
            preparedStmt.execute();            
            //resultSetObj = stmtObj.executeQuery(sql);
            connObj.close();
            log(" insert OK");
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        }
        return "schoolsList";
    }  
    
	// Method To Delete The School Details From The Database
	public String deleteSchoolById(int schoolId) throws SQLException{		
		return deleteSchoolDetails(schoolId);		
	}

	// Method To Navigate User To The Edit Details Page And Passing Selecting School Id Variable As A Hidden Value
	public String editSchoolDetailsById() {
		editSchoolId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedSchoolId");		
		return "faces/schoolEdit.xhtml";
	}

	// Method To Update The School Details In The Database
	public String updateSchoolDetails(SchoolBean schoolBean) throws SQLException{
		return updateSchoolDetails(Integer.parseInt(schoolBean.getEditSchoolId()), schoolBean.getName());		
	}   
        
	// Method To Delete The Selected School Id From The Database 
	public String deleteSchoolDetails(int schoolId) throws SQLException {

            connObj = getConnection(); 
            String sql = "delete from School where id = ?";
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = connObj.prepareStatement(sql);
            preparedStmt.setInt    (1,schoolId);

            // execute the preparedstatement
            preparedStmt.execute();            
            //resultSetObj = stmtObj.executeQuery(sql);
            connObj.close();
            log(" delete OK");
		return "faces/schoolsList.xhtml?faces-redirect=true";
	}

	// Method To Update The School Details For A Particular School Id In The Database
	public String updateSchoolDetails(int schoolId, String updatedSchoolName) throws SQLException{
		
                //Query queryObj = entityMgrObj.createQuery("UPDATE SchoolEntityManager s SET s.name=:name WHERE s.id= :id");			
                //queryObj.setParameter("id", schoolId);
                //queryObj.setParameter("name", updatedSchoolName);
		
                //stmtObj = getConnection().createStatement();    
                 connObj = getConnection();  
                String sql = "UPDATE School s SET s.name=? WHERE s.id= ?";    
                PreparedStatement preparedStmt = connObj.prepareStatement(sql);
                preparedStmt.setString (1,updatedSchoolName);
                preparedStmt.setInt    (2,schoolId);
                // execute the preparedstatement
                preparedStmt.execute();          
                
		FacesContext.getCurrentInstance().addMessage("editSchoolForm:schoolId", new FacesMessage("School Record #" + schoolId + " Is Successfully Updated In Db"));
		//return "faces/schoolEdit.xhtml";
                return "faces/schoolsList.xhtml";
	}

	// Helper Method 1 - Fetch Maximum School Id From The Database
	private int getMaxSchoolId() throws SQLException{
		int maxSchoolId = 1;
                stmtObj = getConnection().createStatement();    
                resultSetObj = stmtObj.executeQuery("select max(id)+1 as id from School");    
                while(resultSetObj.next()) {  
                    maxSchoolId= resultSetObj.getInt("id");  
                    log(" Id :"+maxSchoolId);
                }   
		return maxSchoolId;
	}

	// Helper Method 2 - Fetch Particular School Details On The Basis Of School Id From The Database
	private boolean isSchoolIdPresent(int schoolId) {
		boolean idResult = false;
		//Query queryObj = entityMgrObj.createQuery("SELECT s FROM SchoolEntityManager s WHERE s.id = :id");
		//queryObj.setParameter("id", schoolId);
		//SchoolEntityManager selectedSchoolId = (SchoolEntityManager) queryObj.getSingleResult();
		//if(selectedSchoolId != null) {
		//	idResult = true;
	//	}
		return idResult;
	}          
}
