public class Transition {
    //three attributes of transition: source,action,destination
    private String source;
    private String destination;
    private String action;

    public Transition(){
        this.source = null;
        this.destination = null;
        this.action = null;
    }
    public Transition(String source, String destination, String action) {
        this.source = source;
        this.destination = destination;
        this.action = action;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    //rewrite toString function
    public String toString(){
        return this.getSource()+"        -"+this.getAction()+"->       "+this.getDestination();
    }
}
