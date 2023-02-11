
package ti4.map_ttpg;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Public Objectives I",
    "Public Objectives II",
    "Secret Objectives",
    "Agenda",
    "Relics",
    "Other"
})
public class TTPGObjectives {

    @JsonProperty("Public Objectives I")
    private List<String> publicObjectivesI = null;
    @JsonProperty("Public Objectives II")
    private List<String> publicObjectivesII = null;
    @JsonProperty("Secret Objectives")
    private List<String> secretObjectives = null;
    @JsonProperty("Agenda")
    private List<String> agenda = null;
    @JsonProperty("Relics")
    private List<String> relics = null;
    @JsonProperty("Other")
    private List<String> other = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("Public Objectives I")
    public List<String> getPublicObjectivesI() {
        return publicObjectivesI;
    }

    @JsonProperty("Public Objectives I")
    public void setPublicObjectivesI(List<String> publicObjectivesI) {
        this.publicObjectivesI = publicObjectivesI;
    }

    @JsonProperty("Public Objectives II")
    public List<String> getPublicObjectivesII() {
        return publicObjectivesII;
    }

    @JsonProperty("Public Objectives II")
    public void setPublicObjectivesII(List<String> publicObjectivesII) {
        this.publicObjectivesII = publicObjectivesII;
    }

    @JsonProperty("Secret Objectives")
    public List<String> getSecretObjectives() {
        return secretObjectives;
    }

    @JsonProperty("Secret Objectives")
    public void setSecretObjectives(List<String> secretObjectives) {
        this.secretObjectives = secretObjectives;
    }

    @JsonProperty("Agenda")
    public List<String> getAgenda() {
        return agenda;
    }

    @JsonProperty("Agenda")
    public void setAgenda(List<String> agenda) {
        this.agenda = agenda;
    }

    @JsonProperty("Relics")
    public List<String> getRelics() {
        return relics;
    }

    @JsonProperty("Relics")
    public void setRelics(List<String> relics) {
        this.relics = relics;
    }

    @JsonProperty("Other")
    public List<String> getOther() {
        return other;
    }

    @JsonProperty("Other")
    public void setOther(List<String> other) {
        this.other = other;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}