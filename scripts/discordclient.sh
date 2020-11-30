#!/bin/bash

until node discord/DiscordAdapter.js kevbot.discord.config.json ; do
  echo "Discord client died with exit code $?. Restarting."
  sleep 1
done

