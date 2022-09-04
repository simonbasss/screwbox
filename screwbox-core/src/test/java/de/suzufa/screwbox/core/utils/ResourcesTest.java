package de.suzufa.screwbox.core.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResourcesTest {

    private record JsonDemo(String name) {
    }

    @Test
    void loadBinary_fileNotFound_throwsException() {
        assertThatThrownBy(() -> Resources.loadBinary("unknown.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("file not found: unknown.jpg");
    }

    @Test
    void loadBinary_fileNameIsNull_throwsException() {
        assertThatThrownBy(() -> Resources.loadBinary(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("fileName must not be null");
    }

    @Test
    void loadBinary_fileFound_loadsContent() {
        byte[] content = Resources.loadBinary("tile.bmp");

        assertThat(content).hasSize(1078);
    }

    @Test
    void loadJson_fileNameNull_throwsException() {
        assertThatThrownBy(() -> Resources.loadJson(null, String.class))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("fileName must not be null");
    }

    @Test
    void loadJson_typeNull_throwsException() {
        assertThatThrownBy(() -> Resources.loadJson("tile.bmp", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type must not be null");
    }

    @Test
    void loadJson_noJson_throwsException() {
        assertThatThrownBy(() -> Resources.loadJson("tile.bmp", String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("tile.bmp is not a JSON-File");
    }

    @Test
    void loadJson_corruptJson_throwsExceptuon() {
        assertThatThrownBy(() -> Resources.loadJson("fake.json", String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("file could not be deserialized: fake.json");
    }

    @Test
    void loadJson_jsonOkay_returnsObject() {
        JsonDemo result = Resources.loadJson("real.json", JsonDemo.class);
        assertThat(result.name).isEqualTo("jason");
    }
}
