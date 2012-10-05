#!/usr/bin/env ruby
Dir.foreach(".") do |fname|
	next unless fname.include?(".svg")
	`convert -crop 227x314+259+369 -transparent white #{fname} #{fname.gsub(".svg", ".png")}`
end
