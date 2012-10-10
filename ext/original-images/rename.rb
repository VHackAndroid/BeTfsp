#!/usr/bin/env ruby
FOLDER="Spades"
Dir.foreach(FOLDER) do |fname|
	next unless fname.include?(".svg")
	`mv #{FOLDER+"/"+fname} #{FOLDER.downcase+"_"+fname.downcase}`
end
