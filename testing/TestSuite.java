import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Specify that this class is a test suite
@RunWith(Suite.class)

// Specify all test classes to include
@Suite.SuiteClasses({
    GamePlayTesting.class,
    PlayerTest.class
})
public class TestSuite {
    // This class remains empty. It is only used as a holder for the above annotations.
}