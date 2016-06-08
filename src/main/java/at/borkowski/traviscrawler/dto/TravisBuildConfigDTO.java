package at.borkowski.traviscrawler.dto;

import at.borkowski.traviscrawler.json.OptionalArrayDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

public class TravisBuildConfigDTO {
    private String language;
    // ignored: notifications
    @JsonDeserialize(using = OptionalArrayDeserializer.class)
    private List<String> script;
    // ignored: .result
    @JsonDeserialize(using = OptionalArrayDeserializer.class)
    private List<String> os;
    private String group;
    @JsonDeserialize(using = OptionalArrayDeserializer.class)
    private List<String> dist;

    public String getLanguage() {
        return language;
    }

    public List<String> getScript() {
        return script;
    }

    public List<String> getOs() {
        return os;
    }

    public String getGroup() {
        return group;
    }

    public List<String> getDist() {
        return dist;
    }
}
