package org.saavy.property;/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;

/**
 *
 * @author rgsaavedra
 */
public abstract class DataBean extends BasicDynaBean {

    private String owner = "";
    private String databaseID = "";
    private boolean initialized = false;
    //private BasicDynaBean backup = null;
    private String id = "";
    //private STATUS status = STATUS.IDLE;
//    private Module module;
    
    //private DatabaseBeanHandler handler;

    protected DataBean(BasicDynaClass clss) {
        super(clss);
    }

//    public void destroyBean(DatabaseController objectController) throws DatabaseException {
//        if(handler != null){
//            handler.destroyBean(objectController, this);
//        }
//    }
//
//    public boolean getFromDatabase(DatabaseController objectController) throws DatabaseException {
//        if(handler != null){
//            return handler.getFromDatabase(objectController, this);
//        }
//        return true;
//    }
//
//    public void initializeBean(DatabaseController objectController) throws DatabaseException {
//        setInitialized(true);
//        if(handler != null){
//            setInitialized(handler.initializeBean(objectController, this));
//        }
//    }
//    
//    public int saveToDatabase(DatabaseController objectController) throws DatabaseException {
//        if(handler != null){
//            return handler.saveToDatabase(objectController, this);
//        }
//        return 0;
//    }
    
    private boolean modifiedOrNew = false;
    
    public boolean isModifiedOrNew() {
        return modifiedOrNew;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    @Override
    public void set(String arg0, Object arg1) {
        
        if(super.get(arg0) == null && arg1 == null){
            return;
        }
        if(super.get(arg0) == null && arg1 != null){
            super.set(arg0, arg1);
            setModifiedOrNew(true);
        }else{
            Object obj = super.get(arg0);
            if(!obj.equals(arg1)){
                super.set(arg0, arg1);
                setModifiedOrNew(true);
            }
        }
    }

    @Override
    public void set(String arg0, String arg1, Object arg2) {
        if(super.get(arg0,arg1) == null && arg2 == null){
            return;
        }
        if(super.get(arg0,arg1) == null && arg2 != null){
            super.set(arg0, arg1, arg2);
            setModifiedOrNew(true);
        }else{
            Object obj = super.get(arg0,arg1);
            if(!obj.equals(arg2)){
                super.set(arg0, arg1, arg2);
                setModifiedOrNew(true);
            }
        }
    }

    @Override
    public void set(String arg0, int arg1, Object arg2) {
        if(super.get(arg0,arg1) == null && arg2 == null){
            return;
        }
        if(super.get(arg0,arg1) == null && arg2 != null){
            super.set(arg0, arg1, arg2);
            setModifiedOrNew(true);
        }else{
            Object obj = super.get(arg0,arg1);
            if(!obj.equals(arg2)){
                super.set(arg0, arg1, arg2);
                setModifiedOrNew(true);
            }
        }
    }
    
    public void backupData() {
        setModifiedOrNew(false);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(String databaseID) {
        this.databaseID = databaseID;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

//    public STATUS getStatus() {
//        return status;
//    }
//
//    public void setStatus(STATUS status) {
//        this.status = status;
//    }

//    public DatabaseBeanHandler getDatabaseBeanHandler() {
//        return handler;
//    }
//
//    public void setDatabaseBeanHandler(DatabaseBeanHandler handler) {
//        this.handler = handler;
//    }

    protected void setModifiedOrNew(boolean modifiedOrNew) {
        this.modifiedOrNew = modifiedOrNew;
    }

//    public Module getModule() {
//        return module;
//    }
//
//    public void setModule(Module module) {
//        this.module = module;
//    }

//    public static enum STATUS {
//
//        REQUESTING,
//        READING,
//        WRITING,
//        IDLE;
//    }
//    ArrayList<DatabaseBeanListener> listeners;
//
//    public void addDatabaseBeanListener(DatabaseBeanListener l) {
//        if (listeners == null) {
//            listeners = new ArrayList<DatabaseBeanListener>();
//        }
//        if(!listeners.contains(l)){
//            listeners.add(l);
//        }
//    }
//
//    public void removeDatabaseBeanListener(DatabaseBeanListener l) {
//        if (listeners != null) {
//            listeners.remove(l);
//        }
//    }
//    
//    private class DatabaseWorker extends SwingWorker{
//        private org.saavy.property.DataBean db;
//        private DatabaseBeanListener l;
//        private String type;
//        
//        public DatabaseWorker(org.saavy.property.DataBean db, DatabaseBeanListener l,String type){
//            this.db = db;
//            this.l = l;
//            this.type = type;
//            invoke();
//        }
//        @Override
//        protected Object doInBackground() throws Exception {
//            if(type.equalsIgnoreCase("normal")){
//                beanUpdate();
//            }else if(type.equalsIgnoreCase("polling")){
//                pollingUpdate();
//            }
//            return null;
//        }
//        public void beanUpdate(){
//            l.beanUpdate(db);
//        }
//        public void pollingUpdate(){
//            l.pollingUpdate(db);
//        }
//        private void invoke(){
//            SwingUtilities.invokeLater(this);
//        }
//        
//    }
//    
//    public void fireBeanUpdate(String listenerID) {
//        if(listenerID != null && this.getModule() != null && this.getModule().getObjectHandler()!=null){
//            Object obj = this.getModule().getObjectHandler().getObject(listenerID);
//            if(obj instanceof DatabaseBeanListener){
//                new DatabaseWorker(this,(DatabaseBeanListener) obj, "normal");
//                return;
//            }
//        }
//        if (listeners != null) {
//            for (DatabaseBeanListener l : listeners) {
//                new DatabaseWorker(this, l, "normal");
//            }
//        }
//    }
//
//    public void firePollingBeanUpdate(String listenerID) {
//        if(listenerID != null && this.getModule() != null && this.getModule().getObjectHandler()!=null){
//            Object obj = this.getModule().getObjectHandler().getObject(listenerID);
//            if(obj instanceof DatabaseBeanListener){
//                new DatabaseWorker(this, (DatabaseBeanListener) obj, "polling");
//                return;
//            }
//        }
//        if (listeners != null) {
//            for (DatabaseBeanListener l : listeners) {
//                new DatabaseWorker(this, l, "polling");
//            }
//        }
//    }
//    
//    public void fireBeanUpdate() {
//        fireBeanUpdate(null);
//    }
//
//    public void firePollingBeanUpdate() {
//        fireBeanUpdate(null);
//    }
//
//    public DatabaseRequest createRequest(String moduleID,TYPE type) {
//        DatabaseRequest req = DatabaseRequest.createRequest(moduleID,this, type);
//        return req;
//    }
}
