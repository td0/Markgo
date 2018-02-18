# Markgo
Report broken road, sad memories, the way she made you feel when you were by her side.

# How-To Deploy
1. Generate your Android Studio keystore, look it up on google if you don't know how to
2. Put your debug.keystore on .signing directory. So it looks like project_root/.signing/debug.keystore
3. Create new [Firebase](https://firebase.google.com) project.
4. Go to your newly created Firebase project, and 'click add firebase to your android app'
5. Android package name is : me.tadho.markgo, just leave the nickname blank and copy your debug keystore on the third form. Hit the 'REGISTER APP' button.
6. Go to Authentication on firebase console navigation menu, click SIGN-IN method tab
7. Enable Phone Sign-in provider
8. Get Google Map API key
