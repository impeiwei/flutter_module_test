

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';


class FirstRouteWidget extends StatelessWidget {

  static const MethodChannel methodChannel = MethodChannel('start_cache_activity');


  @override
  Widget build(BuildContext context) {

    return Scaffold(
      appBar: AppBar(
        title: Text('FirstRouteWidget'),
      ),
      body: Center(
        child: ListView (
          children: [
  
            RaisedButton(
              child: Text('startCacheFlutterActivity'),
              onPressed: () {
                methodChannel.invokeMethod("startActivity");
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
                
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
                print("scroll ...");
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {

              },
            ),
            Text("scroll ..."),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
            Text("scroll ..."),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
            Text('scroll ...'),
            RaisedButton(
              child: Text('scroll ...'),
              onPressed: () {
              },
            ),
          ]
        )
      
      )

    );
  }


}
