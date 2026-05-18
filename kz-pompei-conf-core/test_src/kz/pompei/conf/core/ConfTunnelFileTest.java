package kz.pompei.conf.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfTunnelFileTest {

  @Test public void read() throws IOException {
    Path dir = Files.createTempDirectory("conf-tunnel-file-test");
    Path file = dir.resolve("app.conf");
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

    Conf conf = new ConfTunnelFile(dir).read("app.conf");

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

  @Test public void readMissingFile() throws IOException {
    Path dir = Files.createTempDirectory("conf-tunnel-file-test");

    assertThat(new ConfTunnelFile(dir).read("missing.conf")).isNull();
  }

  @Test public void write() throws IOException {
    Path dir = Files.createTempDirectory("conf-tunnel-file-test");
    Conf conf = new Conf();
    conf.confComments.add("configuration comment");
    conf.confComments.add("second configuration comment");

    ConfParam host = new ConfParam();
    host.comments.add("parameter comment");
    host.name = "host";
    host.valueStr = "localhost";
    conf.params.add(host);

    ConfParam multiline = new ConfParam();
    multiline.name = "multiline";
    multiline.valueStr = "line one\nline two";
    conf.params.add(multiline);

    ConfParam literal = new ConfParam();
    literal.name = "literal";
    literal.valueStr = "literal \\n text";
    conf.params.add(literal);

    ConfParam slash = new ConfParam();
    slash.name = "slash";
    slash.valueStr = "C:\\data\\file";
    conf.params.add(slash);

    new ConfTunnelFile(dir).write("nested/app.conf", conf);

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

    Conf readConf = new ConfTunnelFile(dir).read("nested/app.conf");
    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).containsExactly("configuration comment", "second configuration comment");
    assertThat(readConf.params).hasSize(4);
    assertThat(readConf.params.get(1).valueStr).isEqualTo("line one\nline two");
    assertThat(readConf.params.get(2).valueStr).isEqualTo("literal \\n text");
    assertThat(readConf.params.get(3).valueStr).isEqualTo("C:\\data\\file");
  }

  @Test public void lastModified() throws IOException {
    Path dir = Files.createTempDirectory("conf-tunnel-file-test");
    Path file = dir.resolve("app.conf");
    Files.writeString(file, "");

    Instant lastModified = new ConfTunnelFile(dir).lastModified("app.conf");

    assertThat(lastModified).isNotNull();
    assertThat(lastModified).isEqualTo(Files.getLastModifiedTime(file).toInstant());
    assertThat(new ConfTunnelFile(dir).lastModified("missing.conf")).isNull();
  }

}
