package tfm.uniovi.pirateseas.model.canvasmodel.game.objects;

public class Nature {
    private String natureName;
    private int drawableResource;

    /**
     * Constructor
     * @param name Nature name
     */
    public Nature(String name, int resource){
        this.natureName = name;
        this.drawableResource = resource;
    }

    /**
     * @return the nature name
     */
    public String getNatureName() {
        return natureName;
    }

    /**
     * @return the drawable resource
     */
    public int getDrawableResource() {
        return drawableResource;
    }

    /**
     * @param natureName the nature name to set
     */
    public void setNatureName(String natureName) {
        this.natureName = natureName;
    }

    /**
     * @param drawableResource the drawable resource to set
     */
    public void setDrawableResource(int drawableResource) {
        this.drawableResource = drawableResource;
    }
}
