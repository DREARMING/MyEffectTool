package com.example;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class MyClass {
    private static int VERSION = 1;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION,"com.mvp.myeffecttools.dao");
        createEntity(schema);
        try {
            new DaoGenerator().generateAll(schema,"D:/Applications/Android/projects/Summer_Train/MyEffectTools/app/src/main/java");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void createEntity(Schema schema){
        Entity lockapp = schema.addEntity("LockApp");
        lockapp.addStringProperty("packageName").notNull();
        lockapp.addBooleanProperty("isProtece").notNull();
    }
}
