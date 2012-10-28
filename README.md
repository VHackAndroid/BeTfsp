wePoker
=======

wePoker is a multi-player poker game for Android that allows collocated people to play a game of Poker.
wePoker does not need a connection to the internet, everything happens using local communication technology.
On newer devices, wePoker will even use Wifi-Direct and NFC!

wePoker won the 'best app' award at the V Hack Android Belgium Hackathon in October 2012.
You can see a demo at http://bit.ly/wePokerDemo or download the application at http://bit.ly/wePokerapp.

Features
========
- Nomadic and Spontaneous Poker games!
	- Play poker even when disconnected from the internet or when no free wifi is available.
		This is much more common than expected: trains, Eurostar, airplane, bus, metro, car, at holidays, at the beach, cafe, ...
		And those are actually the places where you have spare time to play poker!
		No set-up or pin code or internet required, just launch the application and start playing with your friends.

- Multi-Device interactivity: max_fun(x)_ { x | x E P{Phones, Tablet, GoogleTV, SmartWatches} }
	- Optimise experience based on available resources at that moment in time.
		For instance:
			- Two _phones_ (or more) can start a local game
			- Add a _tablet_ that can serve as the poker table
			- The _GoogleTV_ serves as a high-end poker table where the audience can spectate at a distance. Pubs can offer a GoogleTV basestation with the opportunity to win beers! Local gaming with a couple of friends also benefit from relaxed living-room environment. Furthermore, the stable connectivity of a GoogleTV allows users to join a game over the internet if wanted.
			- A _SmartWatch_ enables intelligent, personalised poker estimates (i.e. what is the probability I can win this game?)

- Interactivity:
	- _Incognito-mode_: When hiding the phone, your private poker cards will be displayed
	- _Curling cards_: Obtain the slick physical feeling of curling cards
	- _Fold-on-backside_: Automated fold when phone is put on its back
	- _Swipe gestures_: Throwing chips at the table
	- _Speech support_: Google's Speech Recognition (with Levenshtein implementation to cope with bad recognition results)
	- _Talkback_: Text2Speech and customized content descriptions
	- _Haptic feedback_: Immersion's SDK

- Connectivity: Using the latest Android API's
	- With infrastructure (i.e. connected to Wifi):
		- _Automated discovery_ of Poker games on the local Wifi.
	- Without infrastructure:
		- _Wifi-Direct_: Allows users to set up a local game without any infrastructure required (Android 4.1 or better). Our intelligent priority connectivity layer configures all complex Wifi-Direct settings automatically. 
		- _NFC_: Allows users to join a game by simply holding the phone near the tablet (NFC Beam)
		- _RFID_: Allows users to scan an RFID tag to join a specific game
		- _QRCode_: For devices without NFC and Wifi-Direct, we support QRCodes to join a game without having to go to _any_ settings menu.

- Accessibility: we customized our interface for accessibility
	- Check for audio feedback (when the microphone plugged in)
	- Set content description for all UI fields to improve talkback
	- Enforced UI view where all cards are accessible (even if there is a dedicated poker table)
	- Allows visually impaired users to compete with sighted people without game-specific advantages for any side.

About the authors
=================
wePoker is being developed by the AmbientTalk team at the Vrije Universiteit Brussel.
We are a research group that specializes in connectivity and communication for mobile devices, especially on a small scale.
Additionally, our advances in multimodal fusion allows to maximize the use of a large range of sensors and actuators.
Check out our other apps at the Google Play Store:
 - weScribble: A collaborative drawing application for Android devices (Play Store Link: http://bit.ly/RZD9kz)
 - The AmbientTalk interpreter: An interpreter for our language, AmbientTalk. You can read more about the language at http://code.google.com/p/ambienttalk. (Play Store link: http://bit.ly/HM7Kzv)
 - The Midas Engine: Complex Event Processing for Gesture Recognition (http://soft.vub.ac.be/~lhoste/midas)
 - The Mudra Framework: Software Abstractions for Multimodal Interaction (http://soft.vub.ac.be/~lhoste/mudra)
