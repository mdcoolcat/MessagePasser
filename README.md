Usage: 
Change the IP address according to your machine;
Run the main() in MessagePasser with one argument, the localName (alice, bob, etc)

Already done:
basic send/receive; local messageId; ruleCheck

TODO:
1. parse yaml config file (i cannot view the website = =), you can try first in the YamlParser;
2. (ask TA if global unique is needed) mechanism to sync global unique messageId, refer to handout. My current implementation does not work well;
3. Multiple machine check
4. maintain socket for reuse
