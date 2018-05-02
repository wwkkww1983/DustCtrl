package com.grean.dustctrl.hardware;

/**
 * Created by weifeng on 2018/5/2.
 */

public class MainBoardLibs {
    private static MainBoardLibs instance = new MainBoardLibs();
    private static final String[] names ={"主板软件版本V1.0","主板软件版本V1.1"};
    private MainBoardController controller;
    private int name;

    public int getName() {
        return name;
    }

    public void setName(int name) {
        if(controller == null) {
            this.name = name;
            selectController(name);
        }else{
            if(name != this.name){
                this.name = name;
                selectController(name);
            }
        }
    }

    public String[] getNames(){
        return names;
    }

    private void selectController(int name){
        switch (name){
            case 0:
                controller = new MainBoardV1_0();
                break;
            case 1:
                controller = new MainBoardV1_1();
                break;
            default:
                controller = new MainBoardV1_0();
                break;

        }
    }

    public MainBoardController getController(){
        if(controller == null){
            selectController(name);
        }
        return controller;
    }

    public static MainBoardLibs getInstance() {
        return instance;
    }

    private MainBoardLibs(){

    }
}
