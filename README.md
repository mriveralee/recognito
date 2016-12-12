**Recognito : Text Independent Speaker Recognition in Java**
============================================================

## Contact and support

https://groups.google.com/d/forum/recognito 

## What to expect

While the lib truly is in its very early stage of development, it is already functional : out of 500 speaker voices extracted from Ted.com talks, Recognito identifies them all.

DISCLAIMER : the above doesn't mean anything for real life scenarios, please read on :-)

Indeed, the Ted based test is quite biased : 
- overall good quality recordings
- professional speakers usually speak loud and clear
- vocal samples (both training and identifying ones) were extracted from a single recording session, which means the surrounding noise and the average volume of the voice remains stable

So the voice print extraction works as advertised but "probably" won't be able to cope with vocal samples of the same speaker coming from various recording systems with huge differences and/or very different sounding environments. 

Please note, I used the word "probably" so basic testing on your side should rapidly provide better insight on whether or not the current state of Recognito is suitable for your particular use case.

There are already people out there quite satisfied with the results... I'm a perfectionist aiming for state-of-the-art technology :-)

## Beyond functionality : the initial goals

The reason why I started this project is that in 2014, AFAICT, there are no Speaker Recognition FOSS available that would meet at least the first 4 criteria of the following list :
- **Available in the form of a library** so you could add this new feature to your app
- **Easy on the user** : short learning curve
- **Fit for usage in a multithreaded environment** (e.g. a web server)
- Using a **permissive licensing model** (I.e. not requiring your app to be OSS as well)
- Keeping an eye on **memory footprint**
- Keeping an eye on **processing efficiency**
- **Written in Java** or a JVM language or providing full JNI hooks

These are mostly software design issues and I wanted to aim at those first before improving on the algorithms. 

## Usage

```java
// Create a new Recognito instance defining the audio sample rate to be used
Recognito<String> recognito = new Recognito<>(16000.0f);

VoicePrint print = recognito.createVoicePrint("Elvis", new File("OldInterview.wav"));

// handle persistence the way you want, e.g.:
// myUser.setVocalPrint(print);
// userDao.saveOrUpdate(myUser);
        
// Now check if the King is back
List<MatchResult<String>> matches = recognito.identify(new File("SomeFatGuy.wav"));
MatchResult<String> match = matches.get(0);

if(match.getKey().equals("Elvis")) {
	System.out.println("Elvis is back !!! " + match.getLikelihoodRatio() + "% positive about it...");
}
```

Please note Recognito's likelihood ratio is dependent on the number of voices it knows. With a single known voice, the likelihood will always be 50%.
The more voice prints you add, the more relevant this likelihood becomes...

Admittedly, this should be easy enough when you're using files but it's not the whole story. Please check the API for other voice print extraction methods in case files are not an option for you. The Javadoc should help a lot too...

One missing feature that's high on my TODO list is automatic handling of microphone input : automatically stop when the user stops talking or after a predefined delay.

## About the author
Amaury Crickx : I am by no means a speech processing academic expert, just a Java geek who happens to also be an experienced sound engineer. Hopefully, this project might attract more knowledgeable ppl and I'll see that the software remains usable for regular developers out there. In the meantime, I'm learning a lot from the reference book on the subject : Fundamentals of Speaker Recognition - Homayoon Beigi

So if you happen to have some knowledge of Speaker Recognition and want to help, you're most welcome !

FWIW, I've presented "Voice Print for Dummies" at Devoxx France 2014 with the help of this lib as didactic material. Soon freely available on www.parleys.com... (in French)
---
# Compilation Using Maven
`cd recognito/`
`mvn compile`
`mvn package`


