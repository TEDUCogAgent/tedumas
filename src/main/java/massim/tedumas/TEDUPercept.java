package massim.tedumas;

import eis.iilang.Percept;
import massim.tedumas.agents.Agent;

import java.lang.Math;



public class TEDUPercept {
    public StringBuilder route;
    public int stepID;
    public long timeStamp;
    public Percept percept;

    @Override
    public String toString() {
        return stepID +
                ", " + timeStamp +
                ", " + percept;
    }

    public boolean rightAgent(Agent agent, String agentName){ return agent.getName().equalsIgnoreCase(agentName.substring(6,8)); }

    public String identifyThing() {
        //used for identifying things whether they are entities, obstacles or dispensers
        //a thing percept: "thing(3,1,obstacle,)"
        return percept.getParameters().get(2).toString();
    }

    public String identifyDispenser() {
        //used for identifying things whether they are entities, obstacles or dispensers
        //a thing percept: "thing(3,1,dispenser,b1)"
        return percept.getParameters().get(3).toString();
    }

    public boolean inTheRightPlace(){
        return percept.getParameters().get(1).toString().equals("0");
    }
    public boolean inTheRightPlace2(){
        return percept.getParameters().get(1).toString().equals("0")
                &&Math.abs(Integer.parseInt(percept.getParameters().get(0).toString()))==2;
    }
    public boolean inTheRightPlace3(){
        return percept.getParameters().get(1).toString().equals("0")
                &&Math.abs(Integer.parseInt(percept.getParameters().get(0).toString()))==1;
    }
    public boolean isSuitableForRequest(){
        if (percept.getName().equals("role") && percept.getParameters().size()==1)
            return percept.getParameters().get(0).toString().equals("worker") || percept.getParameters().get(0).toString().equals("constructor");
        return false;
    }

    public boolean isRoleDefault(){
        if (percept.getName().equals("role") && percept.getParameters().size()==1)
            return percept.getParameters().get(0).toString().equals("default");
        return false;
    }

    public boolean isRoleWorker(){
        if (percept.getName().equals("role") && percept.getParameters().size()==1)
            return percept.getParameters().get(0).toString().equals("worker");
        return false;
    }

    public boolean isSomethingAttached(){
        return percept.getName().equals("attached");
    }

    public boolean inRoleZone(){
        return Integer.parseInt(percept.getParameters().get(0).toString())==0
                &&
                Integer.parseInt(percept.getParameters().get(1).toString())==0;
    }

    public boolean nearDispenser(){ // -1,0
        return (Math.abs(Integer.parseInt(percept.getParameters().get(0).toString()))==1 &&
                Math.abs(Integer.parseInt(percept.getParameters().get(1).toString()))==0 )
                        ||
                (Math.abs(Integer.parseInt(percept.getParameters().get(0).toString()))==0 &&
                Math.abs(Integer.parseInt(percept.getParameters().get(1).toString()))==1);
    }

    public String dispenserWay(){
        char c = 0;
        if(Integer.parseInt(percept.getParameters().get(0).toString())==1)
            c = 'e';
        else if(Integer.parseInt(percept.getParameters().get(0).toString())==-1)
            c = 'w';
        else if(Integer.parseInt(percept.getParameters().get(1).toString())==1)
            c = 's';
        else if(Integer.parseInt(percept.getParameters().get(1).toString())==-1)
            c = 'n';

        return Character.toString(c);
    }

    public Boolean isRequestSuccessful(){
        if (percept.getName().equals("role") && percept.getParameters().size()==1)
            return percept.getParameters().get(0).toString().equals("worker") || percept.getParameters().get(0).toString().equals("constructor");
        return false;
    }
}
