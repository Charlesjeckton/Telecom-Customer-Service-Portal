package model;

public class Service {

    private int id;
    private String name;
    private String description;
    private double charge;       // <-- Matches DB field
    private boolean active;

    // Duration fields
    private Integer durationValue;   // e.g. 30
    private String durationUnit;     // e.g. DAYS / WEEKS / HOURS

    // ----------------------------
    // Constructors
    // ----------------------------
    public Service() {
        this.active = true;
    }

    public Service(int id, String name, String description, double charge,
                   Integer durationValue, String durationUnit, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.charge = charge;
        this.durationValue = durationValue;
        this.durationUnit = durationUnit;
        this.active = active;
    }

    public Service(String name, String description, double charge,
                   Integer durationValue, String durationUnit) {
        this.name = name;
        this.description = description;
        this.charge = charge;
        this.durationValue = durationValue;
        this.durationUnit = durationUnit;
        this.active = true;
    }

    // ----------------------------
    // Getters / Setters
    // ----------------------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCharge() {
        return charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", charge=" + charge +
                ", active=" + active +
                ", durationValue=" + durationValue +
                ", durationUnit='" + durationUnit + '\'' +
                '}';
    }
}
