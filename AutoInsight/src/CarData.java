import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;


public class CarData {
    String State;
    String Plate;
    String Make;
    String Model;
    String Year;
    String BodyType;
    String Transmission;
    String Trim;


    public CarData(String state, String plate) {
        this.State = state;
        this.Plate = plate;
        FetchData();
    }

    private void FetchData(){
        String Url = "https://findbyplate.com/US/"+this.State+"/"+this.Plate+"/";

        try {
            Document document = Jsoup.connect(Url).get();
            Elements info = document.getElementsByClass("cell");
            for(Element element : info) {
                for(Attribute attribute : element.attributes()) {
                    if(attribute.getKey().equals("data-title")){
                        switch(attribute.getValue()){
                            case "Make": this.Make = element.text();
                                break;
                            case "ModelYear": this.Year = element.text();
                                break;
                            case "Model": this.Model = element.text();
                                break;
                            case "NCSABodyType": this.BodyType = element.text();
                                break;
                            case "TransmissionStyle": this.Transmission = element.text();
                                break;
                            case "Trim": this.Trim = element.text();
                                break;
                            default: break;
                        }
                    }
                }
            }
        } catch(IOException e) {
            System.out.println("Plate not found");
            e.printStackTrace();
        }
    }

    public String AllData(){
        String Data;
        Data = this.Year+" "+this.Make+" "+this.Model+" "+this.Trim+" "+this.BodyType+" "+this.Transmission;
        return Data;
    }

    public String Model() {return this.Model;}
    public String Make() {return this.Make;}
    public String Year() {return this.Year;}
    public String Transmission() {return this.Transmission;}
    public String BodyStyle() {return this.BodyType;}
    public String Trim() {return this.Trim;}

}