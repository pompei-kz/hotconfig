package kz.pompei.conf.core;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import lombok.NonNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTunnelFileTest {

  Path dir;

  @BeforeMethod
  public void prepareDir(@NonNull Method testMethod) {
    dir = Paths.get("build/test-data/" + getClass().getSimpleName() + "/" + testMethod);
  }

  @Test public void read() throws IOException {
    Path file = dir.resolve("app.conf");
    file.toFile().getParentFile().mkdirs();
    //noinspection SpellCheckingInspection
    Files.writeString(
      file,
      """
        #configuration comment
        #second configuration comment
        
        #parameter comment
        host=localhost
        
        multiline=line one\\nline two
        
        literal=literal \\\\n text
        
        slash=C:\\\\data\\\\file
        """,
      StandardCharsets.UTF_8
    );

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    Conf conf = confTunnelFile.read("app.conf");
    //
    //

    assertThat(conf).isNotNull();
    assertThat(conf.confComments).containsExactly("configuration comment", "second configuration comment");
    assertThat(conf.params).hasSize(4);
    //noinspection SequencedCollectionMethodCanBeUsed
    assertThat(conf.params.get(0).comments).containsExactly("parameter comment");
    assertThat(conf.params.get(0).name).isEqualTo("host");
    assertThat(conf.params.get(0).valueStr).isEqualTo("localhost");
    assertThat(conf.params.get(1).name).isEqualTo("multiline");
    assertThat(conf.params.get(1).valueStr).isEqualTo("line one\nline two");
    assertThat(conf.params.get(2).name).isEqualTo("literal");
    assertThat(conf.params.get(2).valueStr).isEqualTo("literal \\n text");
    assertThat(conf.params.get(3).name).isEqualTo("slash");
    assertThat(conf.params.get(3).valueStr).isEqualTo("C:\\data\\file");
  }

  @Test public void readMissingFile() {

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    Conf conf = confTunnelFile.read("missing.conf");
    //
    //

    assertThat(conf).isNull();
  }

  @Test public void read__multiline_param_comment() throws IOException {

    dir.toFile().mkdirs();

    Files.writeString(
      dir.resolve("app.conf"),
      """
        #config comment1
        #config comment2
        #config comment3
        
        #first parameter comment
        #second parameter comment
        #third parameter comment
        host=localhost
        """,
      StandardCharsets.UTF_8
    );

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    Conf conf = confTunnelFile.read("app.conf");
    //
    //

    assertThat(conf).isNotNull();
    assertThat(conf.params).hasSize(1);
    //noinspection SequencedCollectionMethodCanBeUsed
    assertThat(conf.params.get(0).comments).containsExactly(
      "first parameter comment",
      "second parameter comment",
      "third parameter comment"
    );
    assertThat(conf.params.get(0).name).isEqualTo("host");
    assertThat(conf.params.get(0).valueStr).isEqualTo("localhost");
  }

  @Test public void write() throws IOException {

    Conf conf = new Conf();
    conf.confComments.add("configuration comment");
    conf.confComments.add("second configuration comment");

    ConfParam host = new ConfParam();
    host.comments.add("parameter comment");
    host.name     = "host";
    host.valueStr = "localhost";
    conf.params.add(host);

    ConfParam multiline = new ConfParam();
    multiline.name     = "multiline";
    multiline.valueStr = "line one\nline two";
    conf.params.add(multiline);

    ConfParam literal = new ConfParam();
    literal.name     = "literal";
    literal.valueStr = "literal \\n text";
    conf.params.add(literal);

    ConfParam slash = new ConfParam();
    slash.name     = "slash";
    slash.valueStr = "C:\\data\\file";
    conf.params.add(slash);

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    confTunnelFile.write("nested/app.conf", conf);
    //
    //

    //noinspection SpellCheckingInspection
    assertThat(Files.readString(dir.resolve("nested/app.conf"), StandardCharsets.UTF_8)).isEqualTo(
      """
        #configuration comment
        #second configuration comment
        
        #parameter comment
        host=localhost
        
        multiline=line one\\nline two
        
        literal=literal \\\\n text
        
        slash=C:\\\\data\\\\file
        """
    );

    Conf readConf = new ConfigTunnelFile(dir).read("nested/app.conf");
    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).containsExactly("configuration comment", "second configuration comment");
    assertThat(readConf.params).hasSize(4);
    assertThat(readConf.params.get(1).valueStr).isEqualTo("line one\nline two");
    assertThat(readConf.params.get(2).valueStr).isEqualTo("literal \\n text");
    assertThat(readConf.params.get(3).valueStr).isEqualTo("C:\\data\\file");
  }

  @Test public void modificationMarker() throws IOException {

    Path file = dir.resolve("app.conf");

    file.toFile().getParentFile().mkdirs();
    Files.writeString(file, "");

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    Long lastModified = confTunnelFile.modificationMarker("app.conf");
    //
    //

    assertThat(lastModified).isNotNull();
    assertThat(lastModified).isEqualTo(Files.getLastModifiedTime(file).toMillis());
  }

  @Test public void modificationMarkerMissingFile() {

    ConfigTunnelFile confTunnelFile = new ConfigTunnelFile(dir);

    //
    //
    Long lastModified = confTunnelFile.modificationMarker("missing.conf");
    //
    //

    assertThat(lastModified).isNull();
  }

}
