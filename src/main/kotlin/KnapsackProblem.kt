fun main(args: Array<String>) {
    val kp = KnapsackProblem()

    kp.makePopulation()

    println("=========================================================")
    println("INITIAL GENERATION:")
    printPopulation(kp)
    kp.evalPopulation(kp.population)
    printFitness(kp)
    kp.getSummary(0)
    kp.makeGenerations()
}

private fun printPopulation(kp: KnapsackProblem) {
    println("\nPopulation:")
    kp.population.forEachIndexed { index, elem -> println("${index + 1} - $elem") }
}

private fun printFitness(kp: KnapsackProblem) {
    println("\nFitness:")
    kp.fitness.forEachIndexed { index, elem -> println("${index + 1} - $elem") }
}

class KnapsackProblem {
    /**
     * Setup
     */
    private val knapsackCapacity: Double = 100.0
    private var populationSize: Int = 20
    private var maxGenerations: Int = 200
    private val crossoverProbability: Double = 0.5
    private val mutationProbability: Double = 0.3

    /**
     * Init variables
     */
    val population: MutableList<String> = mutableListOf()
    private var totalGenerationFitness: Double = 0.0
    var fitness: MutableList<Double> = mutableListOf()
    private val bestGenerationSolution: MutableList<String> = mutableListOf()
    private val bestGenerationFitness: MutableList<Double> = mutableListOf()
    private val averageGenerationFitness: MutableList<Double> = mutableListOf()
    private var mutationCount: Int = 0
    private var crossoverCount: Int = 0
    private var cloneCount: Int = 0
    private var generationCounter: Int = 1
    private val breedPopulation: MutableList<String> = mutableListOf()
    private val items = Item.getItems()

    /**
     * Filling population with random genes
     */
    fun makePopulation() {
        for (i in 0 until populationSize) {
            population.add(generateGene())
        }
    }

    /**
     * Generates gene - random String of 1s and 0s
     */
    private fun generateGene(): String {
        var gene = ""
        for (i in 0 until items.size) {
            gene += (0..2).random().toString()
        }
        return gene
    }

    /**
     * Evaluates population fitness
     */
    fun evalPopulation(population: MutableList<String>) {
        totalGenerationFitness = 0.0
        for (i in 0 until populationSize) {
            val tmpFitness = evalGene(population[i])
            fitness.add(tmpFitness)
            totalGenerationFitness += tmpFitness
        }
    }

    /**
     * Evaluates a single gene's fitness
     */
    private fun evalGene(gene: String): Double {
        var totalWeight = 0.0
        var totalValue = 0.0
        var fitnessValue = 0.0
        var chromosome = '0'

        // totalWeight of items in this gene
        items.forEachIndexed { index, item ->
            chromosome = gene[index]
            if (chromosome == '1') {
                totalWeight += item.weight
                totalValue += item.value
            }
        }

        // if gene items doesn't fit in knapsack fitnessValue is by default 0.0
        if (totalWeight < knapsackCapacity) {
            fitnessValue = totalValue
        }
        return fitnessValue
    }

    private val bestSolution: Int
        get() {
            var bestPosition = 0
            var currentFitness = 0.0
            var bestFitness = 0.0
            for (i in 0 until populationSize) {
                currentFitness = evalGene(population[i])
                if (currentFitness > bestFitness) {
                    bestFitness = currentFitness
                    bestPosition = i
                }
            }
            return bestPosition
        }

    private val averageFitness: Double
        get() {
            var totalFitness = 0.0
            var averageFitness = 0.0
            for (i in 0 until populationSize) {
                totalFitness += fitness[i]
            }
            averageFitness = totalFitness / populationSize
            return averageFitness
        }

    fun getSummary(generationIndex: Int) {
        bestGenerationSolution.add(population[bestSolution])
        bestGenerationFitness.add(evalGene(population[bestSolution]))
        averageGenerationFitness.add(averageFitness)
        println("\n-----------------------------------")
        println("| Best solution: ${bestGenerationSolution[generationIndex]}")
        println("| Best fitness: ${bestGenerationFitness[generationIndex]}")
        println("| Average fitness: ${averageGenerationFitness[generationIndex]}")
        println("-----------------------------------")
        println("| Crossover:  $crossoverCount times")
        println("| Cloning:  $cloneCount times")
        println("| Mutation:  $mutationCount times")
        println("-----------------------------------")
    }

    private fun tournamentSelection(): String {
        val randPickOne = (0..populationSize).random()
        val randPickTwo = (0..populationSize).random()
        val randPickThree = (0..populationSize).random()

        val topGene = mutableListOf(
            Pair(population[randPickOne], fitness[randPickOne]),
            Pair(population[randPickTwo], fitness[randPickTwo]),
            Pair(population[randPickThree], fitness[randPickThree])
        )
            .sortedByDescending { it.second }
            .first()

        return topGene.first
    }

    fun makeGenerations() {

        for (i in 1 until maxGenerations) {
            if (checkForStopCriteria(i)) break
            resetCounters()

            while (breedPopulation.size < populationSize) {
                // if 2 genes wont fit into new population just copy best solution from previous generation
                if (populationSize - breedPopulation.size == 1)
                    breedPopulation.add(bestGenerationSolution[generationCounter - 1])
                crossoverGenes(tournamentSelection(), tournamentSelection())
                mutateGene()
            }

            // Clear fitness values of previous generation
            fitness.clear()

            // Evaluate fitness of breed population members
            evalPopulation(breedPopulation)

            // Copy breed_population to population
            for (k in 0 until populationSize) {
                population[k] = breedPopulation[k]
            }

            println("=========================================================")
            println("\nGENERATION ${(i + 1)}")
            printPopulation(this)
            printFitness(this)
            breedPopulation.clear()
            getSummary(i)
        }
    }

    private fun resetCounters() {
        crossoverCount = 0
        cloneCount = 0
        mutationCount = 0
    }

    private fun checkForStopCriteria(i: Int): Boolean {
        if (maxGenerations > 4 && i > 4) {

            // Previous 3 generational average fitness values
            val a = averageGenerationFitness[i - 1]
            val b = averageGenerationFitness[i - 2]
            val c = averageGenerationFitness[i - 3]

            if (a == b && b == c) {
                println("\nStop criteria!")
                return true
            }
        }
        return false
    }

    /**
     * Crossover
     */
    private fun crossoverGenes(geneOne: String, geneTwo: String) {

        val crossoverRand = Math.random()
        if (crossoverRand <= crossoverProbability) {
            crossoverCount += 1
            val crossingPoint = (0..items.size).random()

            // Crossing genes at randomly chosen spot
            val newGeneOne = geneOne.substring(0, crossingPoint) + geneTwo.substring(crossingPoint)
            val newGeneTwo = geneTwo.substring(0, crossingPoint) + geneOne.substring(crossingPoint)

            // Add new genes to breed_population
            breedPopulation.add(newGeneOne)
            breedPopulation.add(newGeneTwo)
        } else {
            cloneCount += 1
            breedPopulation.add(geneOne)
            breedPopulation.add(geneTwo)
        }
    }

    /**
     * Mutation
     */
    private fun mutateGene() {
        val mutationRand = Math.random()
        if (mutationRand <= mutationProbability && breedPopulation.size >= 1) {
            mutationCount += 1
            val mutatedGene: String = breedPopulation[(0..breedPopulation.size).random()]
            val mutationPoint = (0..items.size).random()
            val newGene = if (mutatedGene[mutationPoint] == '0') {
                mutatedGene.replaceRange(mutationPoint, mutationPoint + 1, "1")
            } else
                mutatedGene.replaceRange(mutationPoint, mutationPoint + 1, "0")

            breedPopulation[breedPopulation.indexOf(mutatedGene)] = newGene
        }
    }
}
